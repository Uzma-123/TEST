package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.payment.PaymentDTO;
import com.bookstore.dto.payment.PaymentRequest;
import com.bookstore.manager.PaymentManager;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentManager paymentManager;

    public PaymentController(PaymentManager paymentManager) {
        this.paymentManager = paymentManager;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDTO>> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentDTO dto = paymentManager.processPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", dto));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByOrder(@PathVariable Long orderId) {
        PaymentDTO dto = paymentManager.getPaymentByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
