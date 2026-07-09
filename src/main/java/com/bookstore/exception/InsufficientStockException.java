package com.bookstore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the requested quantity of a book exceeds available stock.
 * Maps to HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String bookTitle, int requested, int available) {
        super(String.format(
                "Insufficient stock for '%s': requested %d, available %d",
                bookTitle, requested, available));
    }
}
