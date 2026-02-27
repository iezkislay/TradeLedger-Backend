package com.store.app.service;

import com.store.app.dto.CreateWorkOrderRequest;
import com.store.app.entity.*;
import com.store.app.enums.*;
import com.store.app.repository.*;
import com.store.app.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepo;
    private final CustomerRepository customerRepo;
    private final BillRepository billRepo;
    private final ReturnNoteRepository returnNoteRepo;
    private final AuthService authService;

    public WorkOrderService(
            WorkOrderRepository workOrderRepo,
            CustomerRepository customerRepo,
            BillRepository billRepo,
            ReturnNoteRepository returnNoteRepo,
            AuthService authService
    ) {
        this.workOrderRepo = workOrderRepo;
        this.customerRepo = customerRepo;
        this.billRepo = billRepo;
        this.returnNoteRepo = returnNoteRepo;
        this.authService = authService;
    }

    // Create Work Order

    @Transactional
    public WorkOrder createWorkOrder(
            CreateWorkOrderRequest request,
            User user
    ) {
        authService.requireBillingOrOwner(user);

        Customer customer = customerRepo.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        String jobCode = generateJobCode();

        WorkOrder wo = new WorkOrder();
        wo.setJobCode(jobCode);
        wo.setCustomer(customer);
        wo.setDescription(request.getDescription());
        wo.setStatus(WorkOrderStatus.OPEN);

        return workOrderRepo.save(wo);
    }

    public WorkOrderSummaryResponse getSummary(UUID workOrderId) {

        WorkOrder wo = workOrderRepo.findById(workOrderId)
                .orElseThrow(() -> new RuntimeException("Work order not found"));

        List<Bill> bills =
                billRepo.findByWorkOrder_IdOrderByCreatedAtAsc(workOrderId);

        BigDecimal totalBilled = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalReturned = BigDecimal.ZERO;
        BigDecimal totalDue = BigDecimal.ZERO;

        List<WorkOrderSummaryResponse.BillSummary> billSummaries = new java.util.ArrayList<>();

        for (Bill bill : bills) {

            // Effective total = totalAmount (since no returns adjusted here)
            BigDecimal effective = bill.getTotalAmount();
            if (effective == null) effective = BigDecimal.ZERO;

            // Amount Paid = Ledger sum
            BigDecimal paid = billRepo.getTotalPaidForBill(bill.getId());
            if (paid == null) paid = BigDecimal.ZERO;

            BigDecimal due = effective.subtract(paid);

            totalBilled = totalBilled.add(effective);
            totalPaid = totalPaid.add(paid);
            totalDue = totalDue.add(due);

            BigDecimal returned =
                    returnNoteRepo.sumNetReturnsByBill(bill.getId());
            if (returned == null) returned = BigDecimal.ZERO;

            totalReturned = totalReturned.add(returned);

            billSummaries.add(
                    new WorkOrderSummaryResponse.BillSummary(
                            bill.getId(),
                            bill.getBillCode(),
                            bill.getCreatedAt(),
                            effective,
                            paid,
                            due
                    )
            );
        }

        return new WorkOrderSummaryResponse(
                wo.getId(),
                wo.getJobCode(),   // ✅ FIXED
                wo.getCustomer().getName(),
                wo.getStatus().name(),
                totalBilled,
                totalPaid,
                totalReturned,
                totalDue,
                billSummaries
        );
    }


    private String generateJobCode() {

        int year = LocalDate.now().getYear();
        long count = workOrderRepo.count() + 1;

        return String.format("JOB-%d-%04d", year, count);
    }
}
