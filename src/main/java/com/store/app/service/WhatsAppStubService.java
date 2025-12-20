package com.store.app.service;

import org.springframework.stereotype.Service;

@Service
public class WhatsAppStubService implements NotificationService {

    @Override
    public void sendWhatsApp(String mobile, String message) {
        System.out.println("📲 WhatsApp to " + mobile);
        System.out.println(message);
        System.out.println("--------------------------------");
    }
}
