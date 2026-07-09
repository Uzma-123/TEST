package com.bookstore.dao;

import com.bookstore.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentDAO extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);
}
