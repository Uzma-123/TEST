package com.bookstore.dao;

import com.bookstore.model.Order;
import com.bookstore.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDAO extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
}
