package com.store.app.repository;

import com.store.app.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {

    boolean existsByJobCode(String jobCode);

}
