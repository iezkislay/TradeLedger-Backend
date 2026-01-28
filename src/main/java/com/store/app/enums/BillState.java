package com.store.app.enums;

public enum BillState {
    ESTIMATE,   // Price quotation only
    ACTIVE,     // Customer confirmed purchase
    SETTLED,    // Ledger balanced (net = 0)
    CLOSED,
    CANCELLED  // Fully immutable
}
