package com.store.app.util;

import com.store.app.entity.Bill;
import com.store.app.enums.BillState;

public final class BillGuards {

    private BillGuards() {}

    public static void ensureNotClosed(Bill bill) {
        if (bill.getState() == BillState.CLOSED) {
            throw new IllegalStateException("Bill is closed and immutable");
        }
    }

    public static void ensureActiveForOps(Bill bill) {
        if (bill.getState() == BillState.ESTIMATE) {
            throw new IllegalStateException("Operation not allowed on estimate bill");
        }
        if (bill.getState() == BillState.CLOSED) {
            throw new IllegalStateException("Bill is closed and immutable");
        }
    }
}
