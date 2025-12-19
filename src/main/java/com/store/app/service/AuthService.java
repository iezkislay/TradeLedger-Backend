package com.store.app.service;

import com.store.app.entity.User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public void requireOwner(User user) {
        if (user == null || user.getRole() == null ||
                !"OWNER".equals(user.getRole().getName())) {
            throw new RuntimeException("OWNER permission required");
        }
    }

    public void requireBillingOrOwner(User user) {
        if (user == null || user.getRole() == null) {
            throw new RuntimeException("User not authenticated");
        }

        String role = user.getRole().getName();
        if (!"OWNER".equals(role) && !"BILLING".equals(role)) {
            throw new RuntimeException("Billing permission required");
        }
    }
}
