package com.purchase_service.Bottle.Drop.System.dto.request;

import java.util.UUID;

public record RecordBarSaleRequest(
        UUID purchaseId,
        UUID visitorId,
        Long beverageTypeId,
        int quantity
) {
}