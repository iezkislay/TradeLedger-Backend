package com.store.app.repository;

import com.store.app.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReturnRepository extends JpaRepository<Return, UUID> {
}
