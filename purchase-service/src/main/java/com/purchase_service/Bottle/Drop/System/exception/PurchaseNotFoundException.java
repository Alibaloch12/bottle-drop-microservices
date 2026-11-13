package com.purchase_service.Bottle.Drop.System.exception;

public class PurchaseNotFoundException extends RuntimeException {

    public PurchaseNotFoundException(Long id) {
        super("Purchase not found with id: " + id);
    }
}
