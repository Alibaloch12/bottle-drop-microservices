package com.purchase_service.Bottle.Drop.System.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePurchaseRequest {

    @NotNull(message = "visitorId is required")
    private Long visitorId;

    @NotNull(message = "barId is required")
    private Long barId;

    @NotEmpty(message = "items must not be empty")
    @Valid
    private List<UpdatePurchaseItemRequest> items;
}
