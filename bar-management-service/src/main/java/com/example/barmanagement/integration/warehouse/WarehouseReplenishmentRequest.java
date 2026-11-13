package com.example.barmanagement.integration.warehouse;

import java.util.UUID;

public record WarehouseReplenishmentRequest(
        Long barReplenishmentRequestId,
        Long barId,
        Long beverageTypeId,
        String beverageName,
        int requestedQuantity
) {
}