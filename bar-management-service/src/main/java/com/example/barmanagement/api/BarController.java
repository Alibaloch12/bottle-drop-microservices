package com.example.barmanagement.api;

import com.example.barmanagement.application.BarApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bars")
public class BarController {

    private final BarApplicationService service;

    public BarController(
            BarApplicationService service
    ) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BarResponse createBar(
            @Valid
            @RequestBody
            CreateBarRequest request
    ) {
        return service.createBar(request);
    }

    @GetMapping
    public List<BarResponse> getAllBars() {
        return service.getAllBars();
    }

    @GetMapping("/{barId}")
    public BarResponse getBar(
            @PathVariable
            Long barId
    ) {
        return service.getBarStatus(barId);
    }

    @PutMapping("/{barId}")
    public BarResponse updateBar(
            @PathVariable
            Long barId,

            @Valid
            @RequestBody
            UpdateBarRequest request
    ) {
        return service.updateBar(
                barId,
                request
        );
    }

    @DeleteMapping("/{barId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBar(
            @PathVariable
            Long barId
    ) {
        service.deleteBar(barId);
    }

    @PostMapping("/{barId}/stocks")
    public BarResponse addStock(
            @PathVariable
            Long barId,

            @Valid
            @RequestBody
            AddStockRequest request
    ) {
        return service.addStock(
                barId,
                request
        );
    }

    @PutMapping(
            "/{barId}/stocks/{beverageTypeId}"
    )
    public BarResponse updateStock(
            @PathVariable
            Long barId,

            @PathVariable
            Long beverageTypeId,

            @Valid
            @RequestBody
            UpdateStockRequest request
    ) {
        return service.updateStock(
                barId,
                beverageTypeId,
                request
        );
    }

    @DeleteMapping(
            "/{barId}/stocks/{beverageTypeId}"
    )
    public BarResponse removeStock(
            @PathVariable
            Long barId,

            @PathVariable
            Long beverageTypeId
    ) {
        return service.removeStock(
                barId,
                beverageTypeId
        );
    }

    /*
     * Internal REST interface used by Purchasing.
     */
    @GetMapping(
            "/{barId}/availability/{beverageTypeId}"
    )
    public AvailabilityResponse getAvailability(
            @RequestHeader("X-Internal-Api-Key")
            String internalApiKey,

            @PathVariable
            Long barId,

            @PathVariable
            Long beverageTypeId
    ) {
        return service.getAvailability(
                internalApiKey,
                barId,
                beverageTypeId
        );
    }

    /*
     * Internal REST interface used by Purchasing
     * after a purchase has been created.
     */
    @PostMapping("/{barId}/sales")
    public SaleResponse recordSale(
            @RequestHeader("X-Internal-Api-Key")
            String internalApiKey,

            @PathVariable
            Long barId,

            @Valid
            @RequestBody
            RecordSaleRequest request
    ) {
        return service.recordSale(
                internalApiKey,
                barId,
                request
        );
    }

    /*
     * Internal REST interface called by Warehouse
     * after beverages have been delivered.
     */
    @PostMapping("/{barId}/replenishment-fulfillments")
    public BarResponse receiveReplenishment(
            @RequestHeader("X-Internal-Api-Key")
            String internalApiKey,

            @PathVariable
            Long barId,

            @Valid
            @RequestBody
            ReceiveReplenishmentRequest request
    ) {
        return service.receiveReplenishment(
                internalApiKey,
                barId,
                request
        );
    }

    @GetMapping(
            "/{barId}/replenishment-requests"
    )
    public List<ReplenishmentRequestResponse>
    getReplenishmentRequests(
            @PathVariable
            Long barId
    ) {
        return service.getReplenishmentRequests(barId);
    }

    @GetMapping("/{barId}/catalog")
    public BarCatalogResponse getCatalog(
            @RequestHeader("X-Internal-Api-Key")
            String internalApiKey,

            @PathVariable
            Long barId
    ) {
        return service.getCatalog(
                internalApiKey,
                barId
        );
    }

    @PostMapping(
            "/{barId}/availability-check"
    )
    public BatchAvailabilityResponse
    checkAvailability(
            @RequestHeader("X-Internal-Api-Key")
            String internalApiKey,

            @PathVariable
            Long barId,

            @Valid
            @RequestBody
            BatchAvailabilityRequest request
    ) {
        return service.checkAvailability(
                internalApiKey,
                barId,
                request
        );
    }
}