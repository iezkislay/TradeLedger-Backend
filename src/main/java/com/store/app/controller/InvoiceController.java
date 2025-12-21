package com.store.app.controller;

import com.store.app.entity.Bill;
import com.store.app.entity.User;
import com.store.app.repository.BillRepository;
import com.store.app.service.AuthService;
import com.store.app.service.InvoicePdfService;
import jakarta.servlet.http.HttpSession;
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
    private final AuthService authService;

    public InvoiceController(
            BillRepository billRepo,
            InvoicePdfService pdfService,
            AuthService authService
    ) {
        this.billRepo = billRepo;
        this.pdfService = pdfService;
        this.authService = authService;
    }

    /**
     * 📄 Download invoice PDF
     * OWNER + BILLING
     */
    @GetMapping("/{billId}")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable UUID billId,
            HttpSession session
    ) {
        User user = authService.getCurrentUser(session);
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }

        // 🔒 OWNER + BILLING
        authService.requireBillingOrOwner(user);

        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        byte[] pdf = pdfService.generateInvoice(bill);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=invoice-" + bill.getBillNumber() + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
