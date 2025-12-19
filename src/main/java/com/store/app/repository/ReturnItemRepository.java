package com.store.app.repository;

import com.store.app.entity.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, UUID> {
}
