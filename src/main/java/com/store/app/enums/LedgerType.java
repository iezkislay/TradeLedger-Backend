package com.store.app.enums;

public enum LedgerType {
    DEBIT,          // bill raised
    CREDIT,         // actual payment received
    RETURN_CREDIT,  // goods returned (reduces liability)
    ADJUSTMENT      // waiver / rounding / discount
}