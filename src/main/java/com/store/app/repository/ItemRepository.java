package com.store.app.repository;

import com.store.app.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    long countByCategory(String category);
}


