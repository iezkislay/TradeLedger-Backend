package com.store.app.service;

import com.store.app.entity.Bill;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BillNotificationService {

    private final BillingService billingService;
    private final PdfService pdfService;
    private final WhatsAppService whatsAppService;

    public BillNotificationService(
            BillingService billingService,
            PdfService pdfService,
            WhatsAppService whatsAppService
    ) {
        this.billingService = billingService;
        this.pdfService = pdfService;
        this.whatsAppService = whatsAppService;
    }

//    @Async
//    public void sendBillAsync(UUID billId) {
//        try {
//            Bill bill = billingService.getBillEntity(billId);
//
//            if (bill.getCustomer() == null || bill.getCustomer().getMobile() == null) {
//                return;
//            }
//
//            String filePath;
//
//            if (Boolean.TRUE.equals(bill.getIsGstBill())) {
//                filePath = pdfService.generateGstBillPdf(billId);
//            } else {
//                filePath = pdfService.generateNormalBillPdf(billId);
//            }
//
//            // TEMP (needs public URL)
//            String fileUrl = "https://arrah-bihar.com/bills/" + bill.getBillNumber() + ".pdf";
//
//            whatsAppService.sendDocument(bill.getCustomer().getMobile(), fileUrl);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}