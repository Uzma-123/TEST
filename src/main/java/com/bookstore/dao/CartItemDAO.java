package com.bookstore.dao;

import com.bookstore.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemDAO extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId);

    @Transactional
    void deleteByUserId(Long userId);
}
