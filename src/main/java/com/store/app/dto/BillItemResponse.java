package com.store.app.dto;

import com.store.app.entity.BillItem;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class BillItemResponse {

    /* =========================
       EXISTING FIELDS (UNCHANGED)
       ========================= */

    private String itemCode;
    private String itemName;
    private String baseUnit;

    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;

    private BigDecimal fulfilledQty;
    private BigDecimal pendingQty;
    private String fulfilmentStatus;

    /* =========================
       🆕 FIELDS FOR SINGLE VIEW
       ========================= */

    private UUID id;
    private BigDecimal deliveredQty;
    private BigDecimal returnedQty;

    /* =========================
       🆕 NET DERIVED FIELDS
       ========================= */

    private BigDecimal netQuantity;
    private BigDecimal netAmount;

    /* =========================
       🆕 UI-EXPLICIT FIELDS
       ========================= */

    private BigDecimal originalQty;
    private BigDecimal netQty;

    /* =========================
       EXISTING MAPPER (EXTENDED)
       ========================= */

    public static BillItemResponse from(BillItem bi) {

        BillItemResponse r = new BillItemResponse();

        r.setItemCode(bi.getItem().getItemCode());
        r.setItemName(bi.getItem().getName());
        r.setBaseUnit(bi.getItem().getBaseUnit().name());

        // 🔹 Original quantity (never changes)
        r.setQuantity(bi.getQuantity());
        r.setOriginalQty(bi.getQuantity());

        // 🔹 Returned quantity (ERP truth)
        r.setReturnedQty(bi.getReturnedQty());

        // 🔹 Net quantity
        BigDecimal netQty = bi.getNetQuantity();
        r.setNetQuantity(netQty);
        r.setNetQty(netQty);

        // Pricing
        r.setPrice(bi.getPrice());
        r.setAmount(bi.getAmount()); // original amount (KEEP)

        // 🔹 Net amount
        r.setNetAmount(
                netQty.multiply(bi.getPrice())
        );

        r.setFulfilledQty(bi.getFulfilledQty());
        r.setPendingQty(bi.getPendingQty());
        r.setFulfilmentStatus(bi.getFulfilmentStatus());

        return r;
    }

    /* =========================
       🆕 SINGLE BILL-ITEM VIEW
       ========================= */

    public static BillItemResponse fromSingle(BillItem bi) {

        BillItemResponse r = new BillItemResponse();

        r.setId(bi.getId());

        // Item info
        r.setItemCode(bi.getItem().getItemCode());
        r.setItemName(bi.getItem().getName());
        r.setBaseUnit(bi.getItem().getBaseUnit().name());

        // Quantities
        r.setQuantity(bi.getQuantity());
        r.setOriginalQty(bi.getQuantity());
        r.setDeliveredQty(bi.getFulfilledQty());
        r.setFulfilledQty(bi.getFulfilledQty());
        r.setPendingQty(bi.getPendingQty());
        r.setReturnedQty(bi.getReturnedQty());

        BigDecimal netQty = bi.getNetQuantity();
        r.setNetQuantity(netQty);
        r.setNetQty(netQty);

        // Pricing
        r.setPrice(bi.getPrice());
        r.setAmount(bi.getAmount());
        r.setNetAmount(
                netQty.multiply(bi.getPrice())
        );

        // Status
        r.setFulfilmentStatus(bi.getFulfilmentStatus());

        return r;
    }
}
