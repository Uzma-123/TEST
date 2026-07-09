package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.auth.AuthResponse;
import com.bookstore.dto.auth.LoginRequest;
import com.bookstore.dto.auth.RegisterRequest;
import com.bookstore.manager.AuthManager;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles user registration and login.
 *
 * <pre>
 * POST /api/auth/register  — create account, returns JWT (201)
 * POST /api/auth/login     — authenticate, returns JWT (200)
 * </pre>
 *
 * Both endpoints are publicly accessible (no {@code Authorization} header required).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthManager authManager;

    public AuthController(AuthManager authManager) {
        this.authManager = authManager;
    }

    /**
     * Registers a new user account and returns a signed JWT.
     *
     * @param request validated registration fields
     * @return {@code 201 Created} with {@link AuthResponse} payload
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse authResponse = authManager.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    /**
     * Authenticates a user and returns a signed JWT.
     *
     * @param request validated login credentials
     * @return {@code 200 OK} with {@link AuthResponse} payload
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authManager.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authResponse));
    }
}
