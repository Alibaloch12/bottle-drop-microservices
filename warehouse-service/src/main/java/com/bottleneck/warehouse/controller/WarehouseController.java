package com.bottleneck.warehouse.controller;

import com.bottleneck.warehouse.dto.*;
import com.bottleneck.warehouse.entity.BeverageType;
import com.bottleneck.warehouse.entity.RequestStatus;
import com.bottleneck.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/warehouse")
    public WarehouseStatusDTO getWarehouseStatus() {
        return warehouseService.getWarehouseStatus();
    }

    @GetMapping("/warehouse/beverages")
    public List<StockItemDTO> getBeverageStock() {
        return warehouseService.getBeverageStock();
    }

    @GetMapping("/warehouse/empties")
    public List<StockItemDTO> getEmptyStock() {
        return warehouseService.getEmptyStock();
    }

    @PutMapping("/warehouse/beverages/{type}")
    public StockItemDTO updateBeverageStock(
            @PathVariable BeverageType type,
            @Valid @RequestBody UpdateStockRequest request) {
        return warehouseService.updateBeverageStock(type, request.getQuantity());
    }

    @PutMapping("/warehouse/empties/{type}")
    public StockItemDTO updateEmptyStock(
            @PathVariable BeverageType type,
            @Valid @RequestBody UpdateStockRequest request) {
        return warehouseService.updateEmptyStock(type, request.getQuantity());
    }

    @PostMapping("/replenishment-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ReplenishmentRequestDTO createReplenishmentRequest(
            @Valid @RequestBody CreateReplenishmentRequest request) {
        return warehouseService.createReplenishmentRequest(request);
    }

    @GetMapping("/replenishment-requests")
    public List<ReplenishmentRequestDTO> getReplenishmentRequests(
            @RequestParam(required = false) RequestStatus status) {
        return warehouseService.getReplenishmentRequests(status);
    }

    @GetMapping("/replenishment-requests/{id}")
    public ReplenishmentRequestDTO getReplenishmentRequest(@PathVariable Long id) {
        return warehouseService.getReplenishmentRequest(id);
    }

    @PatchMapping("/replenishment-requests/{id}/fulfill")
    public ReplenishmentRequestDTO fulfillReplenishmentRequest(@PathVariable Long id) {
        return warehouseService.fulfillReplenishmentRequest(id);
    }

    @PatchMapping("/replenishment-requests/{id}/reject")
    public ReplenishmentRequestDTO rejectReplenishmentRequest(@PathVariable Long id) {
        return warehouseService.rejectReplenishmentRequest(id);
    }

    @PostMapping("/empty-transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public EmptyTransferDTO receiveEmptyTransfer(@Valid @RequestBody CreateEmptyTransferRequest request) {
        return warehouseService.receiveEmptyTransfer(request);
    }

    @GetMapping("/empty-transfers")
    public List<EmptyTransferDTO> getEmptyTransfers() {
        return warehouseService.getEmptyTransfers();
    }
}
