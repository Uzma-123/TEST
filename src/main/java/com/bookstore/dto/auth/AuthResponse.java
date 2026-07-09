package com.bookstore.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response body returned by both {@code /register} and {@code /login}.
 * Contains the signed JWT and basic user information.
 */
@Data
@Builder
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
}
