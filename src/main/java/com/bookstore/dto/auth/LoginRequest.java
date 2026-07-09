package com.bookstore.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/auth/login}.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
