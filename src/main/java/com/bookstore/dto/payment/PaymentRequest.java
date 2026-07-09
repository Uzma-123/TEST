package com.bookstore.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for processing a payment.
 */
public class PaymentRequest {

    @NotNull(message = "Order id is required")
    private Long orderId;

    @NotBlank(message = "Payment method must not be blank")
    private String paymentMethod;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
