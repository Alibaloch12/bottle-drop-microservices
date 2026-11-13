package com.example.customer_service.api;


import com.example.customer_service.application.CustomerApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
@Validated
public class CustomerController {

    private final CustomerApplicationService service;

    public CustomerController(
            CustomerApplicationService service
    ) {
        this.service = service;
    }

    @GetMapping("/bars/{barId}/catalog")
    public CustomerCatalogResponse getCatalog(
            @PathVariable
            @Positive
            Long barId
    ) {
        return service.getCatalog(barId);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOrderResponse createOrder(
            @Valid
            @RequestBody
            CreateCustomerOrderRequest request
    ) {
        return service.createOrder(request);
    }
}