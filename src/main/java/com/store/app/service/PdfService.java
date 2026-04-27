package com.store.app.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.store.app.dto.BillPrintResponse;
import com.store.app.dto.GstBillPrintResponse;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class PdfService {

    private final BillingService billingService;

    public PdfService(BillingService billingService) {
        this.billingService = billingService;
    }

    public String generateNormalBillPdf(UUID billId) throws Exception {

        BillPrintResponse bill = billingService.getBillForPrint(billId);

        String filePath = "/tmp/bills/" + bill.getBillNumber() + ".pdf";
        Files.createDirectories(Path.of("/tmp/bills/"));

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        document.add(new Paragraph("TAX INVOICE"));
        document.add(new Paragraph("Bill No: " + bill.getBillNumber()));
        document.add(new Paragraph("Date: " + bill.getBillDate()));

        if (bill.getCustomerName() != null) {
            document.add(new Paragraph("Customer: " + bill.getCustomerName()));
        }

        document.add(new Paragraph("-----------------------------"));

        for (var item : bill.getItems()) {
            document.add(new Paragraph(
                    item.getName() + " | " +
                            item.getQuantity() + " " + item.getUnit() +
                            " x " + item.getPrice() +
                            " = " + item.getAmount()
            ));
        }

        document.add(new Paragraph("-----------------------------"));
        document.add(new Paragraph("Total: ₹" + bill.getTotal()));

        document.close();

        return filePath;
    }

    public String generateGstBillPdf(UUID billId) throws Exception {

        GstBillPrintResponse bill = billingService.getGstBillForPrint(billId);

        String filePath = "/tmp/bills/" + bill.getBillNumber() + ".pdf";
        Files.createDirectories(Path.of("/tmp/bills/"));

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        document.add(new Paragraph("TAX INVOICE"));
        document.add(new Paragraph("Bill No: " + bill.getBillNumber()));
        document.add(new Paragraph("Date: " + bill.getBillDate()));

        if (bill.getCustomerName() != null) {
            document.add(new Paragraph("Customer: " + bill.getCustomerName()));
        }
        if (bill.getCustomerGstin() != null) {
            document.add(new Paragraph("GSTIN: " + bill.getCustomerGstin()));
        }

        document.add(new Paragraph("Place of Supply: " + bill.getPlaceOfSupply()));
        document.add(new Paragraph("-----------------------------"));

        for (var item : bill.getItems()) {
            document.add(new Paragraph(
                    item.name + " | " +
                            item.qty + " " + item.baseUnit +
                            " x " + item.rate +
                            " = " + item.amount
            ));
        }

        document.add(new Paragraph("-----------------------------"));
        document.add(new Paragraph("Taxable: ₹" + bill.getTaxableAmount()));
        document.add(new Paragraph("CGST: ₹" + bill.getCgstAmount()));
        document.add(new Paragraph("SGST: ₹" + bill.getSgstAmount()));
        document.add(new Paragraph("Total: ₹" + bill.getTotalAmount()));

        document.close();

        return filePath;
    }
}