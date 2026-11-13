package com.example.barmanagement.integration.warehouse;

import java.util.UUID;

public record WarehouseReplenishmentResponse(
        Long wareHouseReplenishmentRequestId,
        Long barReplenishmentRequestId,
        String status
) {
}