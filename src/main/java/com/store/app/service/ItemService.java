package com.store.app.service;

import com.store.app.entity.Item;
import com.store.app.entity.User;
import com.store.app.enums.ItemCategory;
import com.store.app.repository.ItemRepository;
import com.store.app.repository.StockTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository itemRepo;
    private final StockTransactionRepository stockTxnRepo;
    private final AuthService authService;
    private final AuditService auditService;

    public ItemService(
            ItemRepository itemRepo,
            StockTransactionRepository stockTxnRepo,
            AuthService authService,
            AuditService auditService
    ) {
        this.itemRepo = itemRepo;
        this.stockTxnRepo = stockTxnRepo;
        this.authService = authService;
        this.auditService = auditService;
    }

    // 🔒 OWNER ONLY
    @Transactional
    public Item createItem(Item item, User user) {

        authService.requireOwner(user);

        String prefix =
                ItemCategory.valueOf(item.getCategory()).getPrefix();

        long count =
                itemRepo.countByCategory(item.getCategory()) + 1;

        String code =
                prefix + "-" + String.format("%03d", count);

        item.setItemCode(code);

        return itemRepo.save(item);
    }

    // 🔒 OWNER ONLY
    @Transactional
    public Item updateItem(UUID itemId, Item updated, User user) {

        authService.requireOwner(user);

        Item existing = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        boolean isLocked = stockTxnRepo.existsByItem_Id(itemId);

        if (isLocked) {
            if (!existing.getCategory().equals(updated.getCategory())) {
                throw new RuntimeException("Item category cannot be changed after sale/stock movement");
            }

            if (!existing.getBaseUnit().equals(updated.getBaseUnit())) {
                throw new RuntimeException("Item unit cannot be changed after sale/stock movement");
            }

            if (!existing.getItemCode().equals(updated.getItemCode())) {
                throw new RuntimeException("Item code cannot be changed");
            }
        }

        // 🧾 PRICE CHANGE AUDIT
        if (existing.getSellingPrice() != null &&
                updated.getSellingPrice() != null &&
                existing.getSellingPrice().compareTo(updated.getSellingPrice()) != 0) {

            auditService.log(
                    "ITEM",
                    existing.getId(),
                    "PRICE_CHANGE",
                    existing.getSellingPrice().toString(),
                    updated.getSellingPrice().toString(),
                    user
            );
        }

        // ✅ Allowed updates
        existing.setName(updated.getName());
        existing.setBrand(updated.getBrand());
        existing.setSellingPrice(updated.getSellingPrice());
        existing.setCostPrice(updated.getCostPrice());
        existing.setMinStock(updated.getMinStock());

        return itemRepo.save(existing);
    }
}
