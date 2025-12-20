package com.store.app.controller;

import com.store.app.entity.Bill;
import com.store.app.repository.BillRepository;
import com.store.app.service.InvoicePdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final BillRepository billRepo;
    private final InvoicePdfService pdfService;

    public InvoiceController(BillRepository billRepo, InvoicePdfService pdfService) {
        this.billRepo = billRepo;
        this.pdfService = pdfService;
    }

    @GetMapping("/{billId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable UUID billId) {

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        byte[] pdf = pdfService.generateInvoice(bill);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice-" + bill.getBillNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
