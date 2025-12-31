package com.store.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private Sales sales;
    private List<NamedValue> paymentSplit;
    private List<SalesTrend> salesTrend;
    private List<TopItem> topItems;

    @Data
    @AllArgsConstructor
    public static class Sales {
        private BigDecimal today;
        private BigDecimal month;
        private BigDecimal avgBill;
    }

    @Data
    @AllArgsConstructor
    public static class NamedValue {
        private String name;
        private BigDecimal value;
    }

    @Data
    @AllArgsConstructor
    public static class SalesTrend {
        private LocalDate date;
        private BigDecimal sales;
    }

    @Data
    @AllArgsConstructor
    public static class TopItem {
        private String name;
        private BigDecimal qty;
    }
}
