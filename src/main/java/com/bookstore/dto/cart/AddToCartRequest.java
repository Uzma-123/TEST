package com.bookstore.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adding a book to the authenticated user's cart.
 */
public class AddToCartRequest {

    @NotNull
    private Long bookId;

    @NotNull
    @Min(1)
    private Integer quantity;

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
