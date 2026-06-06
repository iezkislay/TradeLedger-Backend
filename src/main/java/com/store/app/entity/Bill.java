package com.store.app.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.store.app.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import com.store.app.enums.BillState;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "bill_code", unique = true)
    private String billCode;

    @Column(name = "bill_number", nullable = false, unique = true)
    private String billNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @ManyToOne
    private Customer customer;

    /**
     * Sum of all bill item line amounts (inclusive of GST)
     */
    @Column(nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Flat discount applied on bill (₹)
     */
    @Column(nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // ================= GST FIELDS =================

    @Column(name = "is_gst_bill")
    private Boolean isGstBill = false;

    @Column(name = "taxable_amount")
    private BigDecimal taxableAmount = BigDecimal.ZERO;

    @Column(name = "cgst_amount")
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_amount")
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(name = "total_tax")
    private BigDecimal totalTax = BigDecimal.ZERO;

    // ================= BILL STATE =================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillState state = BillState.ACTIVE;

    private LocalDateTime activatedAt;
    private LocalDateTime closedAt;
    private LocalDateTime cancelledAt;

    /**
     * Final payable amount after discount
     */
    @Column(nullable = false)
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    // Bill Items
    @OneToMany(mappedBy = "bill", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("lineNumber ASC")
    @JsonManagedReference
    private List<BillItem> items = new ArrayList<>();

    // ================= HELPER METHODS =================

    public boolean isEstimate() {
        return state == BillState.ESTIMATE;
    }

    public boolean isActive() {
        return state == BillState.ACTIVE;
    }

    public boolean isClosed() {
        return state == BillState.CLOSED;
    }

    public boolean isCancelled() {
        return state == BillState.CANCELLED;
    }
}