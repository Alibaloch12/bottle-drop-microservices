package com.example.barmanagement.api;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchAvailabilityRequest(

        @NotEmpty
        @Size(max = 20)
        List<@Valid Item> items
) {

    public record Item(

            @NotNull
            @Positive
            Long beverageTypeId,

            @Positive
            int quantity
    ) {
    }
}