package com.store.app.repository;

import com.store.app.entity.ItemPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemPriceHistoryRepository
        extends JpaRepository<ItemPriceHistory, UUID> {
}
