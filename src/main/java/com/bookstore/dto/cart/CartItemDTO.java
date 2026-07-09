package com.bookstore.dto.cart;

import java.math.BigDecimal;

/**
 * Response DTO representing a single item in the authenticated user's cart.
 *
 * <p>{@code subtotal} is computed as {@code unitPrice × quantity}.</p>
 */
public class CartItemDTO {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookImageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }

    public String getBookImageUrl() { return bookImageUrl; }
    public void setBookImageUrl(String bookImageUrl) { this.bookImageUrl = bookImageUrl; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
