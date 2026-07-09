package com.bookstore.dto.order;

import java.math.BigDecimal;

/**
 * Response DTO representing a single line item within an order.
 *
 * <p>{@code subtotal} is computed as {@code unitPrice × quantity}.</p>
 */
public class OrderItemDTO {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
