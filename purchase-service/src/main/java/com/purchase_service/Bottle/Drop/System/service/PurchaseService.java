package com.purchase_service.Bottle.Drop.System.service;

import com.purchase_service.Bottle.Drop.System.dto.request.CreatePurchaseRequest;
import com.purchase_service.Bottle.Drop.System.dto.request.RecordBarSaleRequest;
import com.purchase_service.Bottle.Drop.System.dto.request.UpdatePurchaseRequest;
import com.purchase_service.Bottle.Drop.System.dto.response.PurchaseResponse;
import com.purchase_service.Bottle.Drop.System.entity.BeverageTypeInfo;
import com.purchase_service.Bottle.Drop.System.entity.Purchase;
import com.purchase_service.Bottle.Drop.System.entity.PurchaseItem;
import com.purchase_service.Bottle.Drop.System.exception.InvalidReferenceException;
import com.purchase_service.Bottle.Drop.System.exception.PurchaseNotFoundException;
import com.purchase_service.Bottle.Drop.System.exception.ResourceNotFoundException;
import com.purchase_service.Bottle.Drop.System.mapper.PurchaseMapper;
import com.purchase_service.Bottle.Drop.System.repository.PurchaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class PurchaseService {

    private final PurchaseMapper purchaseMapper;
    private final PurchaseRepository PurchaseRepository;
    private final BarServiceClient barServiceClient;

    public PurchaseResponse createPurchase(CreatePurchaseRequest purchaseRequest) {

        Purchase purchase = Purchase.builder()
                .visitorId(purchaseRequest.getVisitorId())
                .barId(purchaseRequest.getBarId())
                .timestamp(LocalDateTime.now())
                .build();

        List<PurchaseItem> purchaseItemList = purchaseMapper.toPurchaseItemList(purchaseRequest.getItems());

        purchaseItemList.forEach(item -> item.setPurchase(purchase));
        purchase.setItems(purchaseItemList);

        // Integration with bar-service is intentionally disabled for now.

        // Intended behavior:
        // 1. For each purchase item, purchase-service should call bar-service
        //    to record the sale and decrement stock.
        // 2. Only after all stock decrements succeed should the purchase be saved.

        // The current bar-service API contract is not compatible with the purchase-service model for IDs as it
        // is used as UUID in the bar-service and enabling this code before the shared contract is fixed would
        // cause integration failures.

//        UUID purchaseId = UUID.randomUUID();
//        UUID visitorId = UUID.fromString(purchaseRequest.getVisitorId().toString());

//        for (PurchaseItem item : purchaseItemList) {
//            barServiceClient.recordSale(
//                    purchaseRequest.getBarId(),
//                    new RecordBarSaleRequest(
//                            purchaseId,
//                            visitorId,
//                            item.getBeverageType().getBeverageTypeId(),
//                            item.getQuantity()
//                    )
//            );
//        }

        purchase.setTotalAmount(purchase.calculateTotalAmount());

        Purchase savedPurchase = PurchaseRepository.save(purchase);

        return purchaseMapper.toPurchaseResponse(savedPurchase);
    }

    public PurchaseResponse getPurchaseById(Long id) {
        Purchase purchase = PurchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));

        return purchaseMapper.toPurchaseResponse(purchase);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseResponse> getPurchases(Long visitorId, Long barId, Pageable pageable) {
        Page<Purchase> purchases;

        if (visitorId != null && barId != null) {
            purchases = PurchaseRepository.findByVisitorIdAndBarId(visitorId, barId, pageable);
        } else if (visitorId != null) {
            purchases = PurchaseRepository.findByVisitorId(visitorId, pageable);
        } else if (barId != null) {
            purchases = PurchaseRepository.findByBarId(barId, pageable);
        } else {
            purchases = PurchaseRepository.findAll(pageable);
        }

        return purchases.map(purchaseMapper::toPurchaseResponse);
    }

    public void deletePurchase(Long id) {
        Purchase purchase = PurchaseRepository.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        PurchaseRepository.delete(purchase);
    }

    @Transactional
    public PurchaseResponse updatePurchase(Long id, UpdatePurchaseRequest request) {
        Purchase existingPurchase = PurchaseRepository.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));

        existingPurchase.setVisitorId(request.getVisitorId());
        existingPurchase.setBarId(request.getBarId());

        List<PurchaseItem> updatedItems = request.getItems().stream()
                .map(itemRequest -> {
                    PurchaseItem item = new PurchaseItem();
                    BeverageTypeInfo beverageTypeInfo = BeverageTypeInfo.builder()
                            .beverageTypeId(itemRequest.getBeverageTypeId())
                            .name(itemRequest.getBeverageName())
                            .description(itemRequest.getBeverageDescription())
                            .build();

                    item.setBeverageType(beverageTypeInfo);
                    item.setQuantity(itemRequest.getQuantity());
                    item.setPriceAtPurchase(itemRequest.getPriceAtPurchase());
                    item.setPurchase(existingPurchase);
                    return item;
                })
                .toList();

        existingPurchase.getItems().clear();
        existingPurchase.getItems().addAll(updatedItems);

        BigDecimal totalAmount = updatedItems.stream()
                .map(item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        existingPurchase.setTotalAmount(totalAmount);

        Purchase updatedPurchase = PurchaseRepository.save(existingPurchase);
        return purchaseMapper.toPurchaseResponse(updatedPurchase);
    }
}
