package com.bookstore.config;

import com.bookstore.dao.UserDAO;
import com.bookstore.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads a {@link User} by email for Spring Security authentication.
 * Role names stored in {@code ERole} (e.g. {@code ROLE_USER}) map
 * directly to {@link SimpleGrantedAuthority} instances.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDAO userDAO;

    public UserDetailsServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * {@inheritDoc}
     *
     * @param email used as the username identifier
     * @throws UsernameNotFoundException if no user exists with that email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
