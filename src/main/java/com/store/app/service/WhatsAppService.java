package com.store.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WhatsAppService {

    @Value("${whatsapp.api.url}")
    private String apiUrl;

    @Value("${whatsapp.phone.number.id}")
    private String phoneNumberId;

    @Value("${whatsapp.access.token}")
    private String accessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * ✅ SEND BILL LINK (CURRENT USE CASE)
     */
    public void sendBillTemplate(String mobile, String customerName, String billCode, UUID billId) {

        try {
            String cleanMobile = sanitizeMobile(mobile);

            String url = apiUrl + "/" + phoneNumberId + "/messages";

            String link = "https://api.arrah-bihar.com/public/bills/" + billId + "/pdf";

            String body = """
        {
          "messaging_product": "whatsapp",
          "to": "%s",
          "type": "template",
          "template": {
            "name": "billing",
            "language": {
              "code": "en"
            },
            "components": [
              {
                "type": "body",
                "parameters": [
                  { "type": "text", "text": "%s" },
                  { "type": "text", "text": "%s" },
                  { "type": "text", "text": "%s" }
                ]
              }
            ]
          }
        }
        """.formatted(
                    "91" + cleanMobile,
                    customerName != null ? customerName : "Customer",
                    billCode,
                    link
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            System.out.println("WhatsApp TEMPLATE sent: " + response.getBody());

        } catch (Exception e) {
            System.err.println("WhatsApp TEMPLATE failed: " + e.getMessage());
        }
    }

    /**
     * Temporary template
     */
    public void sendHelloWorldTemplate(String mobile) {

        try {
            String cleanMobile = sanitizeMobile(mobile);

            String url = apiUrl + "/" + phoneNumberId + "/messages";

            String body = """
        {
          "messaging_product": "whatsapp",
          "to": "%s",
          "type": "template",
          "template": {
            "name": "hello_world",
            "language": {
              "code": "en_US"
            }
          }
        }
        """.formatted("91" + cleanMobile);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            System.out.println("WhatsApp HELLO sent: " + response.getBody());

        } catch (Exception e) {
            System.err.println("WhatsApp HELLO failed: " + e.getMessage());
        }
    }

    /**
     * 📄 SEND PDF (FUTURE USE)
     */
    public void sendDocument(String mobile, String fileUrl) {

        try {
            String cleanMobile = sanitizeMobile(mobile);

            String url = apiUrl + "/" + phoneNumberId + "/messages";

            String body = """
            {
              "messaging_product": "whatsapp",
              "to": "%s",
              "type": "document",
              "document": {
                "link": "%s",
                "filename": "invoice.pdf"
              }
            }
            """.formatted("91" + cleanMobile, fileUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            System.out.println("WhatsApp PDF sent to " + cleanMobile + " | Response: " + response.getBody());

        } catch (Exception e) {
            System.err.println("WhatsApp PDF send failed: " + e.getMessage());
        }
    }

    /**
     * 📱 CLEAN MOBILE
     */
    private String sanitizeMobile(String mobile) {
        if (mobile == null) return "";

        return mobile
                .replaceAll("[^0-9]", "")
                .replaceFirst("^91", "");
    }
}