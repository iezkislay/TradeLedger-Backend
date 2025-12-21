package com.store.app.service;

import com.store.app.entity.User;
import com.store.app.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthService(
            UserRepository userRepo,
            PasswordEncoder encoder
    ) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    /* =========================
       AUTHENTICATION (PHASE-3A)
       ========================= */

    public User login(
            String username,
            String password,
            HttpSession session
    ) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Store logged-in user in session
        session.setAttribute("USER", user);
        return user;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("USER");
    }

    /* =========================
       AUTHORIZATION (RBAC)
       ========================= */

    // 🔒 OWNER only
    public void requireOwner(User user) {
        if (user == null || user.getRole() == null ||
                !"OWNER".equals(user.getRole().getName())) {
            throw new RuntimeException("OWNER permission required");
        }
    }

    // 🔒 OWNER or BILLING
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
