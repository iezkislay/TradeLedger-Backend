package com.store.app.enums;

public enum ReferenceType {
    BILL,           // Bill creation
    PAYMENT,        // Cash / UPI / Card received
    RETURN,         // Goods returned (no cash yet)
    REFUND,         // Cash / UPI paid back
    ADJUSTMENT,
    MANUAL,         // Manual stock adjustment
    PRICE_OVERRIDE
}