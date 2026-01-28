package com.store.app.util;

import com.store.app.entity.Bill;
import com.store.app.entity.BillItem;

public class WhatsAppTemplates {

    public static String billCreated(Bill bill) {
        return """
        🧾 *Puja Hardware*

        Bill No: %s
        Amount: ₹%s

        Thank you for your purchase!
        """.formatted(
                bill.getBillNumber(),
                bill.getTotalAmount()
        );
    }

    public static String estimate(Bill bill) {
        return """
        📄 *Estimate from Puja Hardware*

        Estimate No: %s
        Date: %s
        Amount: ₹%s

        This is only a price estimate.
        Final bill will be generated after confirmation.
        """.formatted(
                bill.getBillNumber(),
                bill.getCreatedAt().toLocalDate(),
                bill.getTotalAmount()
        );
    }


    public static String pendingItems(Bill bill) {
        return """
        ⏳ *Pending Items Reminder*

        Bill No: %s
        Some items are pending.
        We’ll notify once ready.
        """.formatted(bill.getBillNumber());
    }

    public static String fulfilmentDone(BillItem item) {
        return """
        ✅ *Items Ready*

        Item: %s
        Bill No: %s

        Please collect from shop.
        """.formatted(
                item.getItem().getName(),
                item.getBill().getBillNumber()
        );
    }
}
