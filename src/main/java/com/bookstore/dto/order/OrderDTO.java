package com.bookstore.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO representing a customer order with its line items.
 */
public class OrderDTO {

    private Long id;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemDTO> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}
