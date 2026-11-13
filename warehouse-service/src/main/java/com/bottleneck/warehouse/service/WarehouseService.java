package com.bottleneck.warehouse.service;

import com.bottleneck.warehouse.dto.*;
import com.bottleneck.warehouse.entity.*;
import com.bottleneck.warehouse.exception.InsufficientStockException;
import com.bottleneck.warehouse.exception.InvalidRequestStateException;
import com.bottleneck.warehouse.exception.ResourceNotFoundException;
import com.bottleneck.warehouse.integration.BeverageTypeResolver;
import com.bottleneck.warehouse.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final BeverageStockRepository beverageStockRepository;
    private final EmptyStockRepository emptyStockRepository;
    private final ReplenishmentRequestRepository replenishmentRequestRepository;
    private final EmptyTransferRepository emptyTransferRepository;

    public WarehouseService(
            WarehouseRepository warehouseRepository,
            BeverageStockRepository beverageStockRepository,
            EmptyStockRepository emptyStockRepository,
            ReplenishmentRequestRepository replenishmentRequestRepository,
            EmptyTransferRepository emptyTransferRepository) {
        this.warehouseRepository = warehouseRepository;
        this.beverageStockRepository = beverageStockRepository;
        this.emptyStockRepository = emptyStockRepository;
        this.replenishmentRequestRepository = replenishmentRequestRepository;
        this.emptyTransferRepository = emptyTransferRepository;
    }

    @Transactional(readOnly = true)
    public WarehouseStatusDTO getWarehouseStatus() {
        Warehouse warehouse = getCentralWarehouse();
        List<StockItemDTO> beverages = beverageStockRepository.findByWarehouse(warehouse).stream()
                .map(stock -> new StockItemDTO(stock.getBeverageType(), stock.getQuantity()))
                .toList();
        List<StockItemDTO> empties = emptyStockRepository.findByWarehouse(warehouse).stream()
                .map(stock -> new StockItemDTO(stock.getBeverageType(), stock.getQuantity()))
                .toList();
        long pendingCount = replenishmentRequestRepository
                .findByStatusOrderByCreatedAtDesc(RequestStatus.PENDING).size();

        return new WarehouseStatusDTO(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getLocation(),
                beverages,
                empties,
                pendingCount
        );
    }

    @Transactional(readOnly = true)
    public List<StockItemDTO> getBeverageStock() {
        return beverageStockRepository.findByWarehouse(getCentralWarehouse()).stream()
                .map(stock -> new StockItemDTO(stock.getBeverageType(), stock.getQuantity()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockItemDTO> getEmptyStock() {
        return emptyStockRepository.findByWarehouse(getCentralWarehouse()).stream()
                .map(stock -> new StockItemDTO(stock.getBeverageType(), stock.getQuantity()))
                .toList();
    }

    public StockItemDTO updateBeverageStock(BeverageType beverageType, int quantity) {
        BeverageStock stock = getOrCreateBeverageStock(beverageType);
        stock.setQuantity(quantity);
        beverageStockRepository.save(stock);
        return new StockItemDTO(beverageType, quantity);
    }

    public StockItemDTO updateEmptyStock(BeverageType beverageType, int quantity) {
        EmptyStock stock = getOrCreateEmptyStock(beverageType);
        stock.setQuantity(quantity);
        emptyStockRepository.save(stock);
        return new StockItemDTO(beverageType, quantity);
    }

    public BarReplenishmentResponse createBarReplenishmentRequest(BarReplenishmentRequest request) {
        CreateReplenishmentRequest internal = new CreateReplenishmentRequest();
        internal.setBarId(request.barId());
        internal.setBeverageType(
                BeverageTypeResolver.resolve(request.beverageTypeId(), request.beverageName()));
        internal.setQuantity(request.requestedQuantity());

        ReplenishmentRequestDTO created = createReplenishmentRequest(internal);
        return new BarReplenishmentResponse(
                created.id(),
                toBarStatus(created.status()));
    }

    public void receiveDropPointEmpties(DropPointEmptiesTransferRequest request) {
        CreateEmptyTransferRequest internal = new CreateEmptyTransferRequest();
        internal.setDropPointId(request.dropPointId());
        internal.setQuantity(request.quantity());
        // Drop points collect mixed empties; BEER is the shared default type.
        internal.setBeverageType(BeverageType.BEER);
        receiveEmptyTransfer(internal);
    }

    public ReplenishmentRequestDTO createReplenishmentRequest(CreateReplenishmentRequest request) {
        ReplenishmentRequest replenishmentRequest = new ReplenishmentRequest();
        replenishmentRequest.setBarId(request.getBarId());
        replenishmentRequest.setBeverageType(request.getBeverageType());
        replenishmentRequest.setQuantity(request.getQuantity());
        replenishmentRequest.setCreatedAt(LocalDateTime.now());
        replenishmentRequest.setStatus(RequestStatus.PENDING);

        if (hasSufficientBeverageStock(request.getBeverageType(), request.getQuantity())) {
            deductBeverageStock(request.getBeverageType(), request.getQuantity());
            replenishmentRequest.setStatus(RequestStatus.FULFILLED);
        }

        return ReplenishmentRequestDTO.from(replenishmentRequestRepository.save(replenishmentRequest));
    }

    @Transactional(readOnly = true)
    public List<ReplenishmentRequestDTO> getReplenishmentRequests(RequestStatus status) {
        List<ReplenishmentRequest> requests = status == null
                ? replenishmentRequestRepository.findAll()
                : replenishmentRequestRepository.findByStatusOrderByCreatedAtDesc(status);
        return requests.stream().map(ReplenishmentRequestDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public ReplenishmentRequestDTO getReplenishmentRequest(Long id) {
        return ReplenishmentRequestDTO.from(findReplenishmentRequest(id));
    }

    public ReplenishmentRequestDTO fulfillReplenishmentRequest(Long id) {
        ReplenishmentRequest request = findReplenishmentRequest(id);
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStateException("Only pending requests can be fulfilled");
        }
        deductBeverageStock(request.getBeverageType(), request.getQuantity());
        request.setStatus(RequestStatus.FULFILLED);
        return ReplenishmentRequestDTO.from(replenishmentRequestRepository.save(request));
    }

    public ReplenishmentRequestDTO rejectReplenishmentRequest(Long id) {
        ReplenishmentRequest request = findReplenishmentRequest(id);
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStateException("Only pending requests can be rejected");
        }
        request.setStatus(RequestStatus.REJECTED);
        return ReplenishmentRequestDTO.from(replenishmentRequestRepository.save(request));
    }

    public EmptyTransferDTO receiveEmptyTransfer(CreateEmptyTransferRequest request) {
        EmptyTransfer transfer = new EmptyTransfer();
        transfer.setDropPointId(request.getDropPointId());
        transfer.setBeverageType(request.getBeverageType());
        transfer.setQuantity(request.getQuantity());
        transfer.setReceivedAt(LocalDateTime.now());

        EmptyStock stock = getOrCreateEmptyStock(request.getBeverageType());
        stock.setQuantity(stock.getQuantity() + request.getQuantity());
        emptyStockRepository.save(stock);

        return EmptyTransferDTO.from(emptyTransferRepository.save(transfer));
    }

    @Transactional(readOnly = true)
    public List<EmptyTransferDTO> getEmptyTransfers() {
        return emptyTransferRepository.findAllByOrderByReceivedAtDesc().stream()
                .map(EmptyTransferDTO::from)
                .toList();
    }

    private Warehouse getCentralWarehouse() {
        return warehouseRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Central warehouse has not been initialized"));
    }

    private ReplenishmentRequest findReplenishmentRequest(Long id) {
        return replenishmentRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Replenishment request not found: " + id));
    }

    private boolean hasSufficientBeverageStock(BeverageType beverageType, int quantity) {
        return beverageStockRepository.findByWarehouseAndBeverageType(getCentralWarehouse(), beverageType)
                .map(stock -> stock.getQuantity() >= quantity)
                .orElse(false);
    }

    private void deductBeverageStock(BeverageType beverageType, int quantity) {
        BeverageStock stock = beverageStockRepository
                .findByWarehouseAndBeverageType(getCentralWarehouse(), beverageType)
                .orElseThrow(() -> new InsufficientStockException(
                        "No stock available for " + beverageType));

        if (stock.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient " + beverageType + " stock: requested " + quantity
                            + ", available " + stock.getQuantity());
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        beverageStockRepository.save(stock);
    }

    private BeverageStock getOrCreateBeverageStock(BeverageType beverageType) {
        Warehouse warehouse = getCentralWarehouse();
        return beverageStockRepository.findByWarehouseAndBeverageType(warehouse, beverageType)
                .orElseGet(() -> {
                    BeverageStock stock = new BeverageStock();
                    stock.setWarehouse(warehouse);
                    stock.setBeverageType(beverageType);
                    stock.setQuantity(0);
                    return stock;
                });
    }

    private static String toBarStatus(RequestStatus status) {
        return status == RequestStatus.FULFILLED ? "ACCEPTED" : "REJECTED";
    }

    private EmptyStock getOrCreateEmptyStock(BeverageType beverageType) {
        Warehouse warehouse = getCentralWarehouse();
        return emptyStockRepository.findByWarehouseAndBeverageType(warehouse, beverageType)
                .orElseGet(() -> {
                    EmptyStock stock = new EmptyStock();
                    stock.setWarehouse(warehouse);
                    stock.setBeverageType(beverageType);
                    stock.setQuantity(0);
                    return stock;
                });
    }
}
