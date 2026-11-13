package com.example.barmanagement.integration.identity;


public record UserResponse(
        Long userId,
        String name,
        String role,
        boolean active
) {
}