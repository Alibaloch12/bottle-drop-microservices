package com.purchase_service.Bottle.Drop.System.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeverageTypeInfo {

    private Long beverageTypeId;
    private String name;
    private String description;
}