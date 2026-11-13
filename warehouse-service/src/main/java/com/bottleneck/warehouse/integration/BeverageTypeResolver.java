package com.bottleneck.warehouse.integration;

import com.bottleneck.warehouse.entity.BeverageType;

public final class BeverageTypeResolver {

    private BeverageTypeResolver() {
    }

    public static BeverageType resolve(Long beverageTypeId, String beverageName) {
        if (beverageName != null && !beverageName.isBlank()) {
            try {
                return BeverageType.valueOf(
                        beverageName.trim().toUpperCase().replace(' ', '_'));
            } catch (IllegalArgumentException ignored) {
                // Fall back to shared numeric IDs below.
            }
        }

        if (beverageTypeId != null) {
            return switch (beverageTypeId.intValue()) {
                case 1 -> BeverageType.BEER;
                case 2 -> BeverageType.COLA;
                case 3 -> BeverageType.WATER;
                case 4 -> BeverageType.JUICE;
                case 5 -> BeverageType.ENERGY_DRINK;
                default -> throw new IllegalArgumentException(
                        "Unknown beverage type id: " + beverageTypeId);
            };
        }

        throw new IllegalArgumentException("Beverage type is required");
    }
}
