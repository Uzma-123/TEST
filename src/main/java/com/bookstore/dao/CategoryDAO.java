package com.bookstore.dao;

import com.bookstore.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryDAO extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
}
