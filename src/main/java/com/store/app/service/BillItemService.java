package com.store.app.service;

import com.store.app.entity.BillItem;
import com.store.app.repository.BillItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillItemService {

    private final BillItemRepository billItemRepo;

    /* =====================================================
       🧾 GET SINGLE BILL ITEM (DOMAIN ONLY)
       ===================================================== */

    public BillItem getBillItem(UUID billItemId) {

        return billItemRepo.findById(billItemId)
                .orElseThrow(() -> new RuntimeException("Bill item not found"));
    }
}
