package com.store.app.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.store.app.entity.Bill;
import com.store.app.entity.BillItem;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;

@Service
public class InvoicePdfService {

    public byte[] generateInvoice(Bill bill) {

        try {
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("PUJA HARDWARE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            // Bill Info
            document.add(new Paragraph("Bill No: " + bill.getBillNumber()));
            document.add(new Paragraph("Date: " + bill.getCreatedAt()));
            document.add(new Paragraph("Customer: " +
                    (bill.getCustomer() != null ? bill.getCustomer().getName() : "Walk-in")));

            document.add(new Paragraph(" "));
            document.add(new LineSeparator());

            // Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 1, 1, 1});

            addHeader(table, "Item");
            addHeader(table, "Qty");
            addHeader(table, "Rate");
            addHeader(table, "Amount");
            addHeader(table, "Pending");

            for (BillItem bi : bill.getItems()) {

                table.addCell(bi.getItem().getName());
                table.addCell(bi.getQuantity().toString());
                table.addCell(bi.getPrice().toString());
                table.addCell(bi.getAmount().toString());
                table.addCell(bi.getPendingQty().toString());
            }

            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new LineSeparator());

            // Total
            Font totalFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            document.add(new Paragraph(
                    "TOTAL: ₹ " + bill.getTotalAmount(), totalFont
            ));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Invoice generation failed");
        }
    }

    private void addHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
