package com.purchase_service.Bottle.Drop.System.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BarSaleResponse(
        UUID purchaseId,
        UUID barId,
        Long beverageTypeId,
        String beverageName,
        BigDecimal unitPrice,
        int soldQuantity,
        BigDecimal totalPrice,
        int remainingQuantity,
        boolean belowRestockThreshold,
        UUID replenishmentRequestId,
        String replenishmentStatus
) {
}