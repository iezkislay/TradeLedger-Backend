package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * READ-ONLY DTO
 * -----------------------
 * Used for customer statement / ledger timeline.
 * No business logic.
 * No entity exposure.
 * Stable API contract.
 */
@Setter
@Getter
public class CustomerStatementRowDto {

    private LocalDate date;          // Transaction date
    private String reference;        // Bill code or ADJUSTMENT
    private String entryType;        // DEBIT / CREDIT / ADJUSTMENT
    private BigDecimal debit;         // Amount debited
    private BigDecimal credit;        // Amount credited

    public CustomerStatementRowDto(
            LocalDate date,
            String reference,
            String entryType,
            BigDecimal debit,
            BigDecimal credit
    ) {
        this.date = date;
        this.reference = reference;
        this.entryType = entryType;
        this.debit = debit;
        this.credit = credit;
    }

}
