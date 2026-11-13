package com.purchase_service.Bottle.Drop.System.mapper;

import com.purchase_service.Bottle.Drop.System.dto.request.PurchaseItemRequest;
import com.purchase_service.Bottle.Drop.System.dto.response.PurchaseItemResponse;
import com.purchase_service.Bottle.Drop.System.dto.response.PurchaseResponse;
import com.purchase_service.Bottle.Drop.System.entity.BeverageTypeInfo;
import com.purchase_service.Bottle.Drop.System.entity.Purchase;
import com.purchase_service.Bottle.Drop.System.entity.PurchaseItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PurchaseMapper {

    public PurchaseItem toPurchaseItem(PurchaseItemRequest request) {
        if (request == null) {
            return null;
        }

        BeverageTypeInfo beverageTypeInfo = BeverageTypeInfo.builder()
                .beverageTypeId(request.getBeverageTypeId())
                .name(request.getBeverageName())
                .description(request.getBeverageDescription())
                .build();

        return PurchaseItem.builder()
                .beverageType(beverageTypeInfo)
                .quantity(request.getQuantity())
                .priceAtPurchase(request.getPriceAtPurchase())
                .build();
    }

    public List<PurchaseItem> toPurchaseItemList(List<PurchaseItemRequest> requests) {
        if (requests == null) {
            return Collections.emptyList();
        }

        return requests.stream()
                .map(this::toPurchaseItem)
                .collect(Collectors.toList());
    }

    public PurchaseItemResponse toPurchaseItemResponse(PurchaseItem item) {
        if (item == null) {
            return null;
        }

        return PurchaseItemResponse.builder()
                .id(item.getId())
                .beverageTypeId(item.getBeverageType() != null ? item.getBeverageType().getBeverageTypeId() : null)
                .beverageName(item.getBeverageType() != null ? item.getBeverageType().getName() : null)
                .beverageDescription(item.getBeverageType() != null ? item.getBeverageType().getDescription() : null)
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .build();
    }

    public List<PurchaseItemResponse> toPurchaseItemResponseList(List<PurchaseItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }

        return items.stream()
                .map(this::toPurchaseItemResponse)
                .collect(Collectors.toList());
    }

    public PurchaseResponse toPurchaseResponse(Purchase purchase) {
        if (purchase == null) {
            return null;
        }

        return PurchaseResponse.builder()
                .id(purchase.getId())
                .visitorId(purchase.getVisitorId())
                .barId(purchase.getBarId())
                .timestamp(purchase.getTimestamp())
                .totalAmount(purchase.getTotalAmount())
                .items(toPurchaseItemResponseList(purchase.getItems()))
                .build();
    }
}