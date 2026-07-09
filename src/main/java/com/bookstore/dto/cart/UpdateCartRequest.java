package com.bookstore.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating the quantity of an existing cart item.
 */
public class UpdateCartRequest {

    @NotNull
    @Min(1)
    private Integer quantity;

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
