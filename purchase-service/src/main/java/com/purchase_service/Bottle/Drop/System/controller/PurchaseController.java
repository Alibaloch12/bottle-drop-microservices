package com.purchase_service.Bottle.Drop.System.controller;

import com.purchase_service.Bottle.Drop.System.dto.request.CreatePurchaseRequest;
import com.purchase_service.Bottle.Drop.System.dto.response.PurchaseResponse;
import com.purchase_service.Bottle.Drop.System.service.PurchaseService;
import com.purchase_service.Bottle.Drop.System.dto.request.UpdatePurchaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchases")
@Tag(name = "Purchases", description = "Purchase management APIs")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @Operation(summary = "Create a purchase", description = "Creates a new purchase with purchase items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Purchase created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(
            @Valid @RequestBody CreatePurchaseRequest request) {
        PurchaseResponse response = purchaseService.createPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get purchase by id", description = "Returns a purchase for the given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase found"),
            @ApiResponse(responseCode = "404", description = "Purchase not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getPurchaseById(
            @Parameter(description = "Purchase id", example = "1")
            @PathVariable Long id) {
        PurchaseResponse response = purchaseService.getPurchaseById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get purchases", description = "Returns all purchases or filters them by visitorId and/or barId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchases returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })

    @GetMapping
    public ResponseEntity<Page<PurchaseResponse>> getPurchases(
            @Parameter(description = "Filter by visitor id", example = "1")
            @RequestParam(required = false) Long visitorId,
            @Parameter(description = "Filter by bar id", example = "2")
            @RequestParam(required = false) Long barId,
            Pageable pageable) {
        Page<PurchaseResponse> responses = purchaseService.getPurchases(visitorId, barId, pageable);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Delete purchase", description = "Deletes a purchase by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Purchase deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Purchase not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchase(
            @Parameter(description = "Purchase id", example = "1")
            @PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update purchase", description = "Updates an existing purchase by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "404", description = "Purchase not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseResponse> updatePurchase(
            @Parameter(description = "Purchase id", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdatePurchaseRequest request) {

        PurchaseResponse response = purchaseService.updatePurchase(id, request);
        return ResponseEntity.ok(response);
    }
}
