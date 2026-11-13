package com.example.customer_service.application;


import com.example.customer_service.api.CustomerCatalogResponse;
import com.example.customer_service.api.CustomerOrderResponse;
import com.example.customer_service.api.CreateCustomerOrderRequest;
import com.example.customer_service.integration.bar.BarCatalogClient;
import com.example.customer_service.integration.bar.BarCatalogResponse;
import com.example.customer_service.integration.purchase.CreatePurchaseRequest;
import com.example.customer_service.integration.purchase.PurchaseClient;
import com.example.customer_service.integration.purchase.PurchaseResponse;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomerApplicationService {

    private final BarCatalogClient barCatalogClient;
    private final PurchaseClient purchaseClient;

    public CustomerApplicationService(
            BarCatalogClient barCatalogClient,
            PurchaseClient purchaseClient
    ) {
        this.barCatalogClient = barCatalogClient;
        this.purchaseClient = purchaseClient;
    }

    public CustomerCatalogResponse getCatalog(
            Long barId
    ) {
        BarCatalogResponse catalog =
                barCatalogClient.getCatalog(barId);

        return new CustomerCatalogResponse(
                catalog.barId(),
                catalog.barName(),
                catalog.location(),
                catalog.products()
                        .stream()
                        .map(product ->
                                new CustomerCatalogResponse.Product(
                                        product.beverageTypeId(),
                                        product.name(),
                                        product.price(),
                                        product.availableQuantity(),
                                        product.available()
                                )
                        )
                        .toList()
        );
    }

    public CustomerOrderResponse createOrder(
            CreateCustomerOrderRequest request
    ) {
        validateNoDuplicateProducts(request);

        CreatePurchaseRequest purchaseRequest =
                new CreatePurchaseRequest(
                        request.visitorId(),
                        request.barId(),
                        request.items()
                                .stream()
                                .map(item ->
                                        new CreatePurchaseRequest.Item(
                                                item.beverageTypeId(),
                                                item.quantity()
                                        )
                                )
                                .toList()
                );

        PurchaseResponse purchase =
                purchaseClient.createPurchase(
                        purchaseRequest
                );

        return new CustomerOrderResponse(
                purchase.purchaseId(),
                purchase.visitorId(),
                purchase.barId(),
                purchase.items()
                        .stream()
                        .map(item ->
                                new CustomerOrderResponse.Item(
                                        item.beverageTypeId(),
                                        item.beverageName(),
                                        item.quantity(),
                                        item.unitPrice(),
                                        item.lineTotal()
                                )
                        )
                        .toList(),
                purchase.totalPrice(),
                purchase.status()
        );
    }

    private void validateNoDuplicateProducts(
            CreateCustomerOrderRequest request
    ) {
        Set<Long> beverageIds =
                new HashSet<>();

        for (
                CreateCustomerOrderRequest.Item item
                : request.items()
        ) {
            if (
                    !beverageIds.add(
                            item.beverageTypeId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "The same beverage type cannot appear " +
                                "more than once in an order: " +
                                item.beverageTypeId()
                );
            }
        }
    }
}