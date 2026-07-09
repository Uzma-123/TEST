package com.bookstore.dao;

import com.bookstore.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemDAO extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    // Check if a user has purchased a specific book (for review eligibility)
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi JOIN oi.order o WHERE o.user.id = :userId AND oi.book.id = :bookId AND o.status = 'DELIVERED'")
    boolean existsByUserIdAndBookIdAndOrderDelivered(@Param("userId") Long userId, @Param("bookId") Long bookId);
}
