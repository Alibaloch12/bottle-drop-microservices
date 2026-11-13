package com.example.barmanagement.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBarRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @NotBlank
        @Size(max = 200)
        String location
) {
}