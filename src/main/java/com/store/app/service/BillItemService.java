package com.store.app.service;

import com.store.app.dto.BillItemResponse;
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
       🧾 GET SINGLE BILL ITEM (READ ONLY)
       ===================================================== */

    public BillItemResponse getBillItem(UUID billItemId) {

        BillItem bi = billItemRepo.findById(billItemId)
                .orElseThrow(() -> new RuntimeException("Bill item not found"));

        return BillItemResponse.fromSingle(bi);
    }
}
