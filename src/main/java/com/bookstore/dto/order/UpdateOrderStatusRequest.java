package com.bookstore.dto.order;

import com.bookstore.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating an order's status (admin-only operation).
 */
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status must not be null")
    private OrderStatus status;

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
