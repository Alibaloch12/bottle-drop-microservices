package com.bottleneck.warehouse.dto;

import com.bottleneck.warehouse.entity.BeverageType;
import com.bottleneck.warehouse.entity.EmptyTransfer;

import java.time.LocalDateTime;

public record EmptyTransferDTO(
        Long id,
        Long dropPointId,
        BeverageType beverageType,
        int quantity,
        LocalDateTime receivedAt
) {
    public static EmptyTransferDTO from(EmptyTransfer transfer) {
        return new EmptyTransferDTO(
                transfer.getId(),
                transfer.getDropPointId(),
                transfer.getBeverageType(),
                transfer.getQuantity(),
                transfer.getReceivedAt()
        );
    }
}
