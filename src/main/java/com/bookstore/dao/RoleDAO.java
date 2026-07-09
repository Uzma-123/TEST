package com.bookstore.dao;

import com.bookstore.model.ERole;
import com.bookstore.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleDAO extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);
}
