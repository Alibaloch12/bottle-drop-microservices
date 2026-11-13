package com.purchase_service.Bottle.Drop.System.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private BeverageTypeInfo beverageType;

    private Integer quantity;

    private BigDecimal priceAtPurchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;
}