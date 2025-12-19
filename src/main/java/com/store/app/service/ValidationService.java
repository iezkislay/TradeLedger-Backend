package com.store.app.service;

import com.store.app.enums.BaseUnit;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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
}
