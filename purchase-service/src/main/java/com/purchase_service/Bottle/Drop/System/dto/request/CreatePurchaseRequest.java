package com.purchase_service.Bottle.Drop.System.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePurchaseRequest {

    @NotNull(message = "Visitor id is required")
    private Long visitorId;

    @NotNull(message = "Bar id is required")
    private Long barId;

    @Valid
    @NotEmpty(message = "Purchase must contain at least one item")
    private List<PurchaseItemRequest> items;
}
