package com.bookstore.transaction;

import com.bookstore.dao.OrderDAO;
import com.bookstore.dao.PaymentDAO;
import com.bookstore.dto.payment.PaymentRequest;
import com.bookstore.exception.PaymentAlreadyProcessedException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Order;
import com.bookstore.model.OrderStatus;
import com.bookstore.model.Payment;
import com.bookstore.model.PaymentStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Transactional
public class PaymentTransaction {

    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;

    public PaymentTransaction(PaymentDAO paymentDAO, OrderDAO orderDAO) {
        this.paymentDAO = paymentDAO;
        this.orderDAO = orderDAO;
    }

    public Payment processPayment(Long orderId, PaymentRequest request) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        paymentDAO.findByOrderId(orderId)
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
                .ifPresent(payment -> {
                    throw new PaymentAlreadyProcessedException("Payment already processed for order: " + orderId);
                });

        Payment payment = Payment.builder()
                .amount(order.getTotalAmount())
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(UUID.randomUUID().toString())
                .paidAt(LocalDateTime.now())
                .order(order)
                .build();

        Payment savedPayment = paymentDAO.save(payment);
        order.setStatus(OrderStatus.CONFIRMED);
        orderDAO.save(order);

        return savedPayment;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentDAO.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
    }
}
