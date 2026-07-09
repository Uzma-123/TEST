package com.bookstore.manager;

import com.bookstore.config.JwtUtil;
import com.bookstore.config.UserDetailsServiceImpl;
import com.bookstore.dto.auth.AuthResponse;
import com.bookstore.dto.auth.LoginRequest;
import com.bookstore.dto.auth.RegisterRequest;
import com.bookstore.model.User;
import com.bookstore.transaction.AuthTransaction;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates authentication business logic.
 *
 * <ul>
 *   <li>{@link #register} — delegates persistence to {@link AuthTransaction},
 *       then issues a JWT for the new user.</li>
 *   <li>{@link #login} — delegates credential verification to Spring Security's
 *       {@link AuthenticationManager}, then issues a JWT for the authenticated user.</li>
 * </ul>
 */
@Service
public class AuthManager {

    private final AuthTransaction authTransaction;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;

    public AuthManager(AuthTransaction authTransaction,
                       JwtUtil jwtUtil,
                       UserDetailsServiceImpl userDetailsService,
                       AuthenticationManager authenticationManager) {
        this.authTransaction = authTransaction;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user and returns a signed JWT along with user details.
     *
     * @param request validated registration payload
     * @return {@link AuthResponse} containing the JWT and user info
     */
    public AuthResponse register(RegisterRequest request) {
        User savedUser = authTransaction.registerUser(request);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return buildAuthResponse(token, savedUser, userDetails);
    }

    /**
     * Authenticates credentials and returns a signed JWT.
     *
     * @param request validated login payload
     * @return {@link AuthResponse} containing the JWT and user info
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = authTransaction.findByEmail(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return buildAuthResponse(token, user, userDetails);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private AuthResponse buildAuthResponse(String token, User user, UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .build();
    }
}
