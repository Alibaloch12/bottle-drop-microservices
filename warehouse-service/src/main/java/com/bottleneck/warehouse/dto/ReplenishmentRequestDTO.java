package com.bottleneck.warehouse.dto;

import com.bottleneck.warehouse.entity.BeverageType;
import com.bottleneck.warehouse.entity.RequestStatus;
import com.bottleneck.warehouse.entity.ReplenishmentRequest;

import java.time.LocalDateTime;

public record ReplenishmentRequestDTO(
        Long id,
        Long barId,
        BeverageType beverageType,
        int quantity,
        RequestStatus status,
        LocalDateTime createdAt
) {
    public static ReplenishmentRequestDTO from(ReplenishmentRequest request) {
        return new ReplenishmentRequestDTO(
                request.getId(),
                request.getBarId(),
                request.getBeverageType(),
                request.getQuantity(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
