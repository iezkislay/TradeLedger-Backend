package com.store.app.controller;

import com.store.app.dto.ApiResponse;
import com.store.app.dto.LoginRequest;
import com.store.app.entity.User;
import com.store.app.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(
            @RequestBody LoginRequest request,
            HttpSession session
    ) {
        User user = authService.login(
                request.getUsername(),
                request.getPassword(),
                session
        );

        // ⚠️ Important: hide password before returning
        user.setPassword(null);

        return ResponseEntity.ok(
                new ApiResponse<>(true, user, "Login successful")
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "OK", "Logged out successfully")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> me(HttpSession session) {

        User user = authService.getCurrentUser(session);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(false, null, "Not logged in")
            );
        }

        user.setPassword(null);

        return ResponseEntity.ok(
                new ApiResponse<>(true, user, "Current user")
        );
    }
}
