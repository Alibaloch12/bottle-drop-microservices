package com.purchase_service.Bottle.Drop.System.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseResponse {

    private Long id;
    private Long visitorId;
    private Long barId;
    private LocalDateTime timestamp;
    private BigDecimal totalAmount;
    private List<PurchaseItemResponse> items;
}
