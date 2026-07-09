package com.bookstore.manager;

import com.bookstore.dao.OrderDAO;
import com.bookstore.dao.UserDAO;
import com.bookstore.dto.payment.PaymentDTO;
import com.bookstore.dto.payment.PaymentRequest;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.exception.UnauthorizedException;
import com.bookstore.model.Order;
import com.bookstore.model.Payment;
import com.bookstore.model.User;
import com.bookstore.transaction.PaymentTransaction;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PaymentManager {

    private final PaymentTransaction paymentTransaction;
    private final UserDAO userDAO;
    private final OrderDAO orderDAO;

    public PaymentManager(PaymentTransaction paymentTransaction, UserDAO userDAO, OrderDAO orderDAO) {
        this.paymentTransaction = paymentTransaction;
        this.userDAO = userDAO;
        this.orderDAO = orderDAO;
    }

    public PaymentDTO processPayment(PaymentRequest request) {
        Long userId = getCurrentUserId();
        Order order = orderDAO.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only pay for your own orders");
        }

        Payment payment = paymentTransaction.processPayment(request.getOrderId(), request);
        return mapToPaymentDTO(payment);
    }

    public PaymentDTO getPaymentByOrder(Long orderId) {
        Long userId = getCurrentUserId();
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only pay for your own orders");
        }

        Payment payment = paymentTransaction.getPaymentByOrderId(orderId);
        return mapToPaymentDTO(payment);
    }

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }

    private PaymentDTO mapToPaymentDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus().name());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setTransactionReference(payment.getTransactionReference());
        dto.setPaidAt(payment.getPaidAt());
        return dto;
    }
}
