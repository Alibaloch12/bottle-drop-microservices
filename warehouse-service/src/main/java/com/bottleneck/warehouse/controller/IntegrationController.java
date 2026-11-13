package com.bottleneck.warehouse.controller;

import com.bottleneck.warehouse.dto.BarReplenishmentRequest;
import com.bottleneck.warehouse.dto.BarReplenishmentResponse;
import com.bottleneck.warehouse.dto.DropPointEmptiesTransferRequest;
import com.bottleneck.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IntegrationController {

    private final WarehouseService warehouseService;

    public IntegrationController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping("/v1/replenishment-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public BarReplenishmentResponse createBarReplenishmentRequest(
            @Valid @RequestBody BarReplenishmentRequest request) {
        return warehouseService.createBarReplenishmentRequest(request);
    }

    @PostMapping("/warehouse/empties-transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveDropPointEmpties(
            @Valid @RequestBody DropPointEmptiesTransferRequest request) {
        warehouseService.receiveDropPointEmpties(request);
    }
}
