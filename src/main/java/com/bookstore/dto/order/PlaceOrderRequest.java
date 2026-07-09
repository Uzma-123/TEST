package com.bookstore.dto.order;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for placing a new order.
 */
public class PlaceOrderRequest {

    @NotBlank(message = "Shipping address must not be blank")
    private String shippingAddress;

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}
