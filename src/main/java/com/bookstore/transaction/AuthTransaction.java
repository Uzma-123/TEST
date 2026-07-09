package com.bookstore.transaction;

import com.bookstore.dao.RoleDAO;
import com.bookstore.dao.UserDAO;
import com.bookstore.dto.auth.RegisterRequest;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.ERole;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Transactional unit of work for authentication operations.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Validate uniqueness of email before registration</li>
 *   <li>Encode the plain-text password</li>
 *   <li>Assign the default {@code ROLE_USER} role</li>
 *   <li>Persist the new {@link User} via {@link UserDAO}</li>
 * </ul>
 */
@Component
@Transactional
public class AuthTransaction {

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final PasswordEncoder passwordEncoder;

    public AuthTransaction(UserDAO userDAO, RoleDAO roleDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user account.
     *
     * @param request validated registration payload
     * @return the persisted {@link User} entity
     * @throws BadRequestException if the email is already registered
     */
    public User registerUser(RegisterRequest request) {
        if (userDAO.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                    "Email is already registered: " + request.getEmail());
        }

        Role userRole = roleDAO.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role", "name", ERole.ROLE_USER));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        return userDAO.save(user);
    }

    /**
     * Loads an existing user by email.
     *
     * @param email the user's registered email address
     * @return the matching {@link User} entity
     * @throws ResourceNotFoundException if no user exists with that email
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userDAO.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", email));
    }
}
