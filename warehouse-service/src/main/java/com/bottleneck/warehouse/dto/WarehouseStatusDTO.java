package com.bottleneck.warehouse.dto;

import java.util.List;

public record WarehouseStatusDTO(
        Long id,
        String name,
        String location,
        List<StockItemDTO> beverages,
        List<StockItemDTO> empties,
        long pendingReplenishmentRequests
) {
}
