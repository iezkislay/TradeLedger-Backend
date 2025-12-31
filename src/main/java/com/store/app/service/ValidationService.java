package com.store.app.service;

import com.store.app.enums.BaseUnit;
import com.store.app.dto.BillItemRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ValidationService {

    public void validateQuantity(BaseUnit unit, BigDecimal qty) {

        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        if (unit == BaseUnit.PCS) {
            if (qty.stripTrailingZeros().scale() > 0) {
                throw new RuntimeException("PCS items must have whole number quantity");
            }
        }
    }

    public void validatePrice(BigDecimal price, BigDecimal costPrice) {

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Selling price must be greater than zero");
        }
    }

    // ✅ NEW METHOD — backward compatible
    public void validateBillItems(List<BillItemRequest> items) {

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Bill must contain at least one item");
        }

        for (BillItemRequest i : items) {

            if (i.getQuantity() == null || i.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Item quantity must be greater than zero");
            }

            if (i.getPrice() == null || i.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Item price must be greater than zero");
            }
        }
    }
}
