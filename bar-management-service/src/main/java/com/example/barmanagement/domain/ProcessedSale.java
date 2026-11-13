package com.example.barmanagement.domain;

import com.example.barmanagement.api.RecordSaleRequest;
import com.example.barmanagement.api.SaleResponse;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "processed_sales")
public class ProcessedSale {

    @Id
    @Column(name = "purchase_id")
    private Long purchaseId;

    @Column(nullable = false)
    private Long visitorId;

    @Column(nullable = false)
    private Long barId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private Instant processedAt;

    @ElementCollection
    @CollectionTable(
            name = "processed_sale_items",
            joinColumns = @JoinColumn(
                    name = "purchase_id"
            )
    )
    @OrderColumn(name = "item_position")
    private List<ProcessedSaleItem> items =
            new ArrayList<>();

    protected ProcessedSale() {
    }

    private ProcessedSale(
            RecordSaleRequest request,
            SaleResponse response
    ) {
        this.purchaseId =
                request.purchaseId();
        this.visitorId =
                request.visitorId();
        this.barId =
                response.barId();
        this.totalPrice =
                response.totalPrice();
        this.processedAt =
                Instant.now();

        this.items =
                response.items()
                        .stream()
                        .map(ProcessedSaleItem::from)
                        .collect(Collectors.toCollection(
                                ArrayList::new
                        ));
    }

    public static ProcessedSale from(
            RecordSaleRequest request,
            SaleResponse response
    ) {
        return new ProcessedSale(
                request,
                response
        );
    }

    public boolean matches(
            Long requestedBarId,
            RecordSaleRequest request
    ) {
        if (
                !Objects.equals(
                        barId,
                        requestedBarId
                )
                        || !Objects.equals(
                        visitorId,
                        request.visitorId()
                )
                        || items.size()
                        != request.items().size()
        ) {
            return false;
        }

        Map<Long, Integer> requestedItems =
                request.items()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        RecordSaleRequest.Item
                                                ::beverageTypeId,
                                        RecordSaleRequest.Item
                                                ::quantity
                                )
                        );

        return items.stream().allMatch(item ->
                Objects.equals(
                        requestedItems.get(
                                item.getBeverageTypeId()
                        ),
                        item.getSoldQuantity()
                )
        );
    }

    public SaleResponse toResponse() {
        return new SaleResponse(
                purchaseId,
                barId,
                items.stream()
                        .map(
                                ProcessedSaleItem::toResponse
                        )
                        .toList(),
                totalPrice
        );
    }
}