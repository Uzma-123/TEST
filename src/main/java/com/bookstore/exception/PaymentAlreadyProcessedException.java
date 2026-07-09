package com.bookstore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a payment is attempted on an order that has already been paid.
 * Maps to HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentAlreadyProcessedException extends RuntimeException {

    public PaymentAlreadyProcessedException(String message) {
        super(message);
    }

    public PaymentAlreadyProcessedException(Long orderId) {
        super(String.format("Payment for order %d has already been processed", orderId));
    }
}
