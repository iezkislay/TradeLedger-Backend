package com.store.app.enums;

public enum LedgerType {
    DEBIT,          // Bill raised
    CREDIT,         // Cash / UPI received
    RETURN_CREDIT,  // Liability reduced due to return
    ADJUSTMENT      // Waiver / rounding
}

