package com.example.barmanagement.application;

import com.example.barmanagement.api.*;
import com.example.barmanagement.common.InternalApiKeyGuard;
import com.example.barmanagement.domain.*;
import com.example.barmanagement.domain.exception.BarNotFoundException;
import com.example.barmanagement.domain.exception.ExternalServiceException;
import com.example.barmanagement.domain.repository.BarRepository;
import com.example.barmanagement.domain.repository.ProcessedSaleRepository;
import com.example.barmanagement.domain.repository.ReplenishmentRequestRepository;
import com.example.barmanagement.integration.identity.IdentityClient;
import com.example.barmanagement.integration.warehouse.WarehouseClient;
import com.example.barmanagement.integration.warehouse.WarehouseReplenishmentRequest;
import com.example.barmanagement.integration.warehouse.WarehouseReplenishmentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Service
public class BarApplicationService {

    private static final int
            DEFAULT_REPLENISHMENT_QUANTITY = 50;

    private final BarRepository barRepository;
    private final ReplenishmentRequestRepository
            requestRepository;
    private final ProcessedSaleRepository processedSaleRepository;
    private final IdentityClient identityClient;
    private final WarehouseClient warehouseClient;
    private final InternalApiKeyGuard internalApiKeyGuard;

    public BarApplicationService(
            BarRepository barRepository,
            ReplenishmentRequestRepository requestRepository,
            ProcessedSaleRepository processedSaleRepository,
            IdentityClient identityClient,
            WarehouseClient warehouseClient,
            InternalApiKeyGuard internalApiKeyGuard
    ) {
        this.barRepository = barRepository;
        this.requestRepository = requestRepository;
        this.processedSaleRepository = processedSaleRepository;
        this.identityClient = identityClient;
        this.warehouseClient = warehouseClient;
        this.internalApiKeyGuard = internalApiKeyGuard;
    }

    @Transactional
    public BarResponse createBar(
            CreateBarRequest request
    ) {

        Bar bar = Bar.create(
                request.name(),
                request.description(),
                request.location()
        );

        return BarResponse.from(
                barRepository.save(bar)
        );
    }

    @Transactional
    public BarResponse updateBar(
            Long barId,
            UpdateBarRequest request
    ) {


        Bar bar = getBar(barId);

        bar.updateDetails(
                request.name(),
                request.description(),
                request.location()
        );

        return BarResponse.from(
                barRepository.save(bar)
        );
    }

    @Transactional
    public void deleteBar(
            Long barId
    ) {


        Bar bar = getBar(barId);

        requestRepository.deleteByBarId(barId);
        barRepository.delete(bar);
    }

    @Transactional
    public BarResponse addStock(
            Long barId,
            AddStockRequest request
    ) {


        Bar bar = getBar(barId);

        BeverageType beverageType =
                BeverageType.of(
                        request.beverageTypeId(),
                        request.beverageName(),
                        request.price()
                );

        bar.addStock(
                beverageType,
                request.initialQuantity(),
                request.restockThreshold()
        );

        return BarResponse.from(
                barRepository.save(bar)
        );
    }

    @Transactional
    public BarResponse updateStock(
            Long barId,
            Long beverageTypeId,
            UpdateStockRequest request
    ) {


        Bar bar = getBar(barId);

        bar.correctStock(
                beverageTypeId,
                request.beverageName(),
                request.price(),
                request.quantity(),
                request.restockThreshold()
        );

        return BarResponse.from(
                barRepository.save(bar)
        );
    }

    @Transactional
    public BarResponse removeStock(
            Long barId,
            Long beverageTypeId
    ) {


        Bar bar = getBar(barId);
        bar.removeStock(beverageTypeId);

        return BarResponse.from(
                barRepository.save(bar)
        );
    }

    @Transactional(readOnly = true)
    public BarResponse getBarStatus(
            Long barId
    ) {
        return BarResponse.from(getBar(barId));
    }

    @Transactional(readOnly = true)
    public List<BarResponse> getAllBars(
    ) {
        return barRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(BarResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(
            String internalApiKey,
            Long barId,
            Long beverageTypeId
    ) {
        internalApiKeyGuard.verify(internalApiKey);

        Bar bar = getBar(barId);

        BeverageType beverageType =
                bar.getBeverageType(beverageTypeId);

        int availableQuantity =
                bar.availableQuantity(beverageTypeId);

        return new AvailabilityResponse(
                barId,
                beverageType.getId(),
                beverageType.getName(),
                beverageType.getPrice(),
                availableQuantity,
                availableQuantity > 0
        );
    }

    @Transactional
    public SaleResponse recordSale(
            String internalApiKey,
            Long barId,
            RecordSaleRequest request
    ) {
        internalApiKeyGuard.verify(
                internalApiKey
        );

        validateUniqueBeverageTypes(
                request.items()
                        .stream()
                        .map(
                                RecordSaleRequest.Item
                                        ::beverageTypeId
                        )
                        .toList()
        );

        ProcessedSale existing =
                processedSaleRepository
                        .findById(
                                request.purchaseId()
                        )
                        .orElse(null);

        if (existing != null) {
            if (
                    !existing.matches(
                            barId,
                            request
                    )
            ) {
                throw new IllegalStateException(
                        "Purchase ID was already used " +
                                "with different sale data."
                );
            }

            return existing.toResponse();
        }


        Bar bar = getBar(barId);

        /*
         * First pass: validate every item.
         * No stock is changed yet.
         */
        for (
                RecordSaleRequest.Item item
                : request.items()
        ) {
            int available =
                    bar.availableQuantity(
                            item.beverageTypeId()
                    );

            if (available < item.quantity()) {
                throw new IllegalStateException(
                        "Insufficient stock for beverage " +
                                item.beverageTypeId() +
                                ". Available: " + available +
                                ", requested: " + item.quantity()
                );
            }
        }

        /*
         * Second pass: all items are valid,
         * therefore stock can be decreased.
         */
        List<SaleResponse.Item> saleItems =
                new java.util.ArrayList<>();

        BigDecimal orderTotal =
                BigDecimal.ZERO.setScale(2);

        for (
                RecordSaleRequest.Item item
                : request.items()
        ) {
            BeverageType beverageType =
                    bar.getBeverageType(
                            item.beverageTypeId()
                    );

            BigDecimal unitPrice =
                    beverageType.getPrice();

            int remainingQuantity =
                    bar.recordSale(
                            item.beverageTypeId(),
                            item.quantity()
                    );

            BigDecimal lineTotal =
                    unitPrice
                            .multiply(
                                    BigDecimal.valueOf(
                                            item.quantity()
                                    )
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            orderTotal =
                    orderTotal.add(lineTotal);

            boolean belowThreshold =
                    bar.isBelowThreshold(
                            item.beverageTypeId()
                    );

            Long replenishmentRequestId =
                    null;

            String replenishmentStatus =
                    "NOT_REQUIRED";

            if (
                    belowThreshold
                            && !hasOpenReplenishment(
                            barId,
                            item.beverageTypeId()
                    )
            ) {
                ReplenishmentRequest replenishment =
                        ReplenishmentRequest.create(
                                barId,
                                beverageType,
                                DEFAULT_REPLENISHMENT_QUANTITY
                        );

                requestRepository.save(
                        replenishment
                );

                replenishmentRequestId =
                        replenishment.getId();

                replenishmentStatus =
                        replenishment
                                .getStatus()
                                .name();

                try {
                    WarehouseReplenishmentResponse
                            warehouseResponse =
                            warehouseClient
                                    .requestReplenishment(
                                            new WarehouseReplenishmentRequest(
                                                    replenishment.getId(),
                                                    barId,
                                                    beverageType.getId(),
                                                    beverageType.getName(),
                                                    replenishment
                                                            .getQuantityRequested()
                                            )
                                    );


                    if (
                            !Objects.equals(
                                    warehouseResponse.barReplenishmentRequestId(),
                                    replenishment.getId()
                            )
                    ) {
                        throw new IllegalStateException(
                                "Warehouse returned an invalid correlation ID."
                        );
                    }

                    if (
                            "ACCEPTED".equalsIgnoreCase(
                                    warehouseResponse.status()
                            )
                    ) {
                        replenishment.markAccepted(
                                warehouseResponse.wareHouseReplenishmentRequestId()
                        );

                    } else if (
                            "REJECTED".equalsIgnoreCase(
                                    warehouseResponse.status()
                            )
                    ) {
                        replenishment.markRejected();

                    } else {
                        throw new IllegalStateException(
                                "Unsupported Warehouse status: " +
                                        warehouseResponse.status()
                        );
                    }

                    replenishmentStatus =
                            replenishment
                                    .getStatus()
                                    .name();

                } catch (
                        ExternalServiceException exception
                ) {
                    replenishmentStatus =
                            "REQUESTED_WAREHOUSE_UNAVAILABLE";
                }

                requestRepository.save(
                        replenishment
                );
            }

            saleItems.add(
                    new SaleResponse.Item(
                            beverageType.getId(),
                            beverageType.getName(),
                            unitPrice,
                            item.quantity(),
                            lineTotal,
                            remainingQuantity,
                            belowThreshold,
                            replenishmentRequestId,
                            replenishmentStatus
                    )
            );
        }

        SaleResponse response =
                new SaleResponse(
                        request.purchaseId(),
                        barId,
                        saleItems,
                        orderTotal
                );

        barRepository.save(bar);

        processedSaleRepository.save(
                ProcessedSale.from(
                        request,
                        response
                )
        );

        return response;
    }

    @Transactional
    public BarResponse receiveReplenishment(
            String internalApiKey,
            Long barId,
            ReceiveReplenishmentRequest request
    ) {
        internalApiKeyGuard.verify(
                internalApiKey
        );

        ReplenishmentRequest replenishment =
                requestRepository
                        .findById(
                                request
                                        .barReplenishmentRequestId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Replenishment request was not found: " +
                                                request
                                                        .barReplenishmentRequestId()
                                )
                        );

        if (
                !Objects.equals(
                        replenishment.getBarId(),
                        barId
                )
        ) {
            throw new IllegalStateException(
                    "The replenishment request belongs " +
                            "to another bar."
            );
        }

        if (
                !Objects.equals(
                        replenishment
                                .getBeverageType()
                                .getId(),
                        request.beverageTypeId()
                )
        ) {
            throw new IllegalStateException(
                    "The delivered beverage type does not " +
                            "match the replenishment request."
            );
        }

        Bar bar = getBar(barId);

        /*
         * Idempotent response:
         * do not increase stock again.
         */
        if (
                replenishment.getStatus()
                        == ReplenishmentStatus.COMPLETED
        ) {
            replenishment.complete(
                    request.warehouseReplenishmentId(),
                    request.deliveredQuantity()
            );

            return BarResponse.from(bar);
        }

        bar.receiveReplenishment(
                request.beverageTypeId(),
                request.deliveredQuantity()
        );

        replenishment.complete(
                request.warehouseReplenishmentId(),
                request.deliveredQuantity()
        );

        requestRepository.save(
                replenishment
        );

        barRepository.save(bar);

        return BarResponse.from(bar);
    }

    @Transactional(readOnly = true)
    public List<ReplenishmentRequestResponse>
    getReplenishmentRequests(
            Long barId
    ) {


        getBar(barId);

        return requestRepository
                .findByBarIdOrderByRequestedAtDesc(
                        barId
                )
                .stream()
                .map(
                        ReplenishmentRequestResponse::from
                )
                .toList();
    }

    private boolean hasOpenReplenishment(
            Long barId,
            Long beverageTypeId
    ) {
        return requestRepository
                .findOpenRequest(
                        barId,
                        beverageTypeId,
                        EnumSet.of(
                                ReplenishmentStatus.REQUESTED,
                                ReplenishmentStatus.ACCEPTED
                        )
                )
                .isPresent();
    }

    private Bar getBar(Long barId) {
        return barRepository
                .findById(barId)
                .orElseThrow(() ->
                        new BarNotFoundException(barId)
                );
    }

    @Transactional(readOnly = true)
    public BarCatalogResponse getCatalog(
            String internalApiKey,
            Long barId
    ) {
        internalApiKeyGuard.verify(internalApiKey);

        Bar bar = getBar(barId);

        return BarCatalogResponse.from(bar);
    }

    @Transactional(readOnly = true)
    public BatchAvailabilityResponse
    checkAvailability(
            String internalApiKey,
            Long barId,
            BatchAvailabilityRequest request
    ) {
        internalApiKeyGuard.verify(
                internalApiKey
        );

        validateUniqueBeverageTypes(
                request.items()
                        .stream()
                        .map(
                                BatchAvailabilityRequest.Item
                                        ::beverageTypeId
                        )
                        .toList()
        );

        Bar bar = getBar(barId);

        List<BatchAvailabilityResponse.Item>
                itemResponses =
                request.items()
                        .stream()
                        .map(item -> {
                            BeverageType beverage =
                                    bar.getBeverageType(
                                            item.beverageTypeId()
                                    );

                            int availableQuantity =
                                    bar.availableQuantity(
                                            item.beverageTypeId()
                                    );

                            return new BatchAvailabilityResponse.Item(
                                    beverage.getId(),
                                    beverage.getName(),
                                    beverage.getPrice(),
                                    item.quantity(),
                                    availableQuantity,
                                    availableQuantity
                                            >= item.quantity()
                            );
                        })
                        .toList();

        boolean allAvailable =
                itemResponses.stream()
                        .allMatch(
                                BatchAvailabilityResponse.Item
                                        ::available
                        );

        return new BatchAvailabilityResponse(
                barId,
                allAvailable,
                itemResponses
        );
    }

    private void validateUniqueBeverageTypes(
            List<Long> beverageTypeIds
    ) {
        java.util.Set<Long> uniqueIds =
                new java.util.HashSet<>();

        for (Long beverageTypeId
                : beverageTypeIds) {

            if (!uniqueIds.add(beverageTypeId)) {
                throw new IllegalArgumentException(
                        "Duplicate beverage type: " +
                                beverageTypeId
                );
            }
        }
    }
}