package com.store.app.service;

import com.store.app.dto.CreateItemRequest;
import com.store.app.dto.ItemSearchResponse;
import com.store.app.entity.Item;
import com.store.app.entity.ItemPriceHistory;
import com.store.app.entity.Stock;
import com.store.app.entity.User;
import com.store.app.enums.BaseUnit;
import com.store.app.enums.ItemCategory;
import com.store.app.repository.ItemPriceHistoryRepository;
import com.store.app.repository.ItemRepository;
import com.store.app.repository.StockRepository;
import com.store.app.repository.StockTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository itemRepo;
    private final StockRepository stockRepo;
    private final StockTransactionRepository stockTxnRepo;
    private final ItemPriceHistoryRepository itemPriceHistoryRepo; // 🆕
    private final AuthService authService;
    private final AuditService auditService;

    public ItemService(
            ItemRepository itemRepo,
            StockRepository stockRepo,
            StockTransactionRepository stockTxnRepo,
            ItemPriceHistoryRepository itemPriceHistoryRepo, // 🆕
            AuthService authService,
            AuditService auditService
    ) {
        this.itemRepo = itemRepo;
        this.stockRepo = stockRepo;
        this.stockTxnRepo = stockTxnRepo;
        this.itemPriceHistoryRepo = itemPriceHistoryRepo; // 🆕
        this.authService = authService;
        this.auditService = auditService;
    }

    /* =====================================================
       ITEM MANAGEMENT (OWNER ONLY)
       ===================================================== */

    @Transactional
    public Item createItem(Item item, User user) {

        authService.requireOwner(user);

        String prefix =
                ItemCategory.valueOf(item.getCategory()).getPrefix();

        long count =
                itemRepo.countByCategory(item.getCategory()) + 1;

        item.setItemCode(prefix + "-" + String.format("%03d", count));

        return itemRepo.save(item);
    }

    @Transactional
    public Item createItemWithOpeningStock(
            CreateItemRequest req,
            User user
    ) {

        authService.requireOwner(user);

        Item item = new Item();
        item.setName(req.getName());
        item.setBrand(req.getBrand());
        item.setCategory(req.getCategory());
        item.setBaseUnit(BaseUnit.valueOf(req.getBaseUnit()));
        item.setCostPrice(req.getCostPrice());
        item.setSellingPrice(req.getSellingPrice());
        item.setMinStock(req.getMinStock());

        String prefix =
                ItemCategory.valueOf(req.getCategory()).getPrefix();

        long count =
                itemRepo.countByCategory(req.getCategory()) + 1;

        item.setItemCode(prefix + "-" + String.format("%03d", count));

        Item savedItem = itemRepo.save(item);

        BigDecimal openingStock =
                req.getOpeningStock() != null
                        ? req.getOpeningStock()
                        : BigDecimal.ZERO;

        if (openingStock.signum() > 0) {
            Stock stock = new Stock();
            stock.setItem(savedItem);   // PK = FK
            stock.setQuantity(openingStock);
            stockRepo.save(stock);
        }

        return savedItem;
    }

    @Transactional
    public Item updateItem(UUID itemId, Item updated, User user) {

        authService.requireOwner(user);

        Item existing = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        boolean isLocked = stockTxnRepo.existsByItem_Id(itemId);

        if (isLocked) {
            if (!existing.getCategory().equals(updated.getCategory())) {
                throw new RuntimeException(
                        "Item category cannot be changed after sale/stock movement"
                );
            }
            if (!existing.getBaseUnit().equals(updated.getBaseUnit())) {
                throw new RuntimeException(
                        "Item unit cannot be changed after sale/stock movement"
                );
            }
            if (!existing.getItemCode().equals(updated.getItemCode())) {
                throw new RuntimeException("Item code cannot be changed");
            }
        }

        /* ================= PRICE CHANGE — AUDIT (EXISTING) ================= */

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

        /* ================= PRICE HISTORY — SELLING (🆕 EXTENSION) ================= */

        if (existing.getSellingPrice() != null &&
                updated.getSellingPrice() != null &&
                existing.getSellingPrice().compareTo(updated.getSellingPrice()) != 0) {

            ItemPriceHistory h = new ItemPriceHistory();
            h.setItem(existing);
            h.setPriceType("SELLING");
            h.setOldPrice(existing.getSellingPrice());
            h.setNewPrice(updated.getSellingPrice());
            h.setChangedBy(user);
            h.setChangedAt(LocalDateTime.now());

            itemPriceHistoryRepo.save(h);
        }

        /* ================= PRICE HISTORY — COST (🆕 EXTENSION) ================= */

        if (existing.getCostPrice() != null &&
                updated.getCostPrice() != null &&
                existing.getCostPrice().compareTo(updated.getCostPrice()) != 0) {

            ItemPriceHistory h = new ItemPriceHistory();
            h.setItem(existing);
            h.setPriceType("COST");
            h.setOldPrice(existing.getCostPrice());
            h.setNewPrice(updated.getCostPrice());
            h.setChangedBy(user);
            h.setChangedAt(LocalDateTime.now());

            itemPriceHistoryRepo.save(h);
        }

        /* ================= APPLY UPDATES ================= */

        existing.setName(updated.getName());
        existing.setBrand(updated.getBrand());
        existing.setSellingPrice(updated.getSellingPrice());
        existing.setCostPrice(updated.getCostPrice());
        existing.setMinStock(updated.getMinStock());

        return itemRepo.save(existing);
    }

    /* =====================================================
       🔍 BILLING — SAFE SEARCH (READ ONLY)
       ===================================================== */

    public List<ItemSearchResponse> searchForBilling(
            String q,
            User user
    ) {

        final boolean showCostPrice = canViewCostPrice(user);

        return itemRepo.search(q, PageRequest.of(0, 20))
                .stream()
                .map(i -> new ItemSearchResponse(
                        i.getId(),
                        i.getItemCode(),
                        i.getName(),
                        i.getBrand(),
                        i.getCategory(),
                        i.getBaseUnit().name(),
                        i.getSellingPrice(),
                        showCostPrice ? i.getCostPrice() : null,
                        i.getMinStock(),
                        stockRepo.findAvailableQty(i.getId())
                ))
                .toList();
    }

    /* =====================================================
       🆕 ITEMS — PAGED LIST / SEARCH (EXISTING)
       ===================================================== */

    public Page<ItemSearchResponse> listItemsPaged(
            String search,
            Pageable pageable,
            User user
    ) {
        return listItemsPaged(
                search,
                null,
                "NAME",
                "ASC",
                pageable,
                user
        );
    }

    /* =====================================================
       🆕 ITEMS — PAGED LIST / SEARCH (ENHANCED)
       ===================================================== */

    public Page<ItemSearchResponse> listItemsPaged(
            String search,
            String category,
            String sortBy,
            String direction,
            Pageable pageable,
            User user
    ) {

        final boolean showCostPrice = canViewCostPrice(user);

        Sort sort = buildSort(sortBy, direction);
        Pageable finalPageable =
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        sort
                );

        Page<Item> page =
                itemRepo.searchPaged(
                        (search == null || search.isBlank())
                                ? null
                                : search.trim(),
                        category,
                        finalPageable
                );

        return page.map(i -> new ItemSearchResponse(
                i.getId(),
                i.getItemCode(),
                i.getName(),
                i.getBrand(),
                i.getCategory(),
                i.getBaseUnit().name(),
                i.getSellingPrice(),
                showCostPrice ? i.getCostPrice() : null,
                i.getMinStock(),
                stockRepo.findAvailableQty(i.getId())
        ));
    }

    /* =====================================================
       📦 LIST ALL ITEMS (EXISTING API — DO NOT BREAK)
       ===================================================== */

    public List<ItemSearchResponse> listAllItems(User user) {
        return listItemsPaged(
                null,
                PageRequest.of(0, Integer.MAX_VALUE),
                user
        ).getContent();
    }

    /* =====================================================
       📦 GET SINGLE ITEM (NEW — SAFE ADDITION)
       ===================================================== */

    public ItemSearchResponse getItemById(UUID itemId, User user) {

        Item item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        boolean showCostPrice = canViewCostPrice(user);

        return new ItemSearchResponse(
                item.getId(),
                item.getItemCode(),
                item.getName(),
                item.getBrand(),
                item.getCategory(),
                item.getBaseUnit().name(),
                item.getSellingPrice(),
                showCostPrice ? item.getCostPrice() : null,
                item.getMinStock(),
                stockRepo.findAvailableQty(item.getId())
        );
    }

    /* =====================================================
       🔧 SORT BUILDER
       ===================================================== */

    private Sort buildSort(String sortBy, String direction) {

        Sort.Direction dir =
                "DESC".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        if ("PRICE".equalsIgnoreCase(sortBy)) {
            return Sort.by(dir, "sellingPrice");
        }

        if ("STOCK".equalsIgnoreCase(sortBy)) {
            return Sort.by(dir, "name");
        }

        return Sort.by(dir, "name");
    }

    /* =====================================================
       🔐 COST PRICE VISIBILITY — SINGLE SOURCE OF TRUTH
       ===================================================== */

    private boolean canViewCostPrice(User user) {
        try {
            authService.requireOwner(user);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
