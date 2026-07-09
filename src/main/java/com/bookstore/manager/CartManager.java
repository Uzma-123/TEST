package com.bookstore.manager;

import com.bookstore.dao.UserDAO;
import com.bookstore.dto.cart.AddToCartRequest;
import com.bookstore.dto.cart.CartItemDTO;
import com.bookstore.dto.cart.UpdateCartRequest;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.CartItem;
import com.bookstore.model.User;
import com.bookstore.transaction.CartTransaction;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates shopping-cart business logic for the authenticated user.
 *
 * <p>Resolves the current user from {@link SecurityContextHolder} and delegates
 * all transactional work to {@link CartTransaction}.</p>
 */
@Service
public class CartManager {

    private final CartTransaction cartTransaction;
    private final UserDAO userDAO;

    public CartManager(CartTransaction cartTransaction, UserDAO userDAO) {
        this.cartTransaction = cartTransaction;
        this.userDAO = userDAO;
    }

    /**
     * Returns all cart items for the authenticated user.
     *
     * @return list of {@link CartItemDTO}s; empty if the cart is empty
     */
    public List<CartItemDTO> getCart() {
        Long userId = getCurrentUserId();
        return cartTransaction.getCartByUser(userId).stream()
                .map(this::mapToCartItemDTO)
                .collect(Collectors.toList());
    }

    /**
     * Adds a book to the authenticated user's cart, or increments the quantity
     * if the item is already present.
     *
     * @param request validated add-to-cart payload
     * @return {@link CartItemDTO} of the saved cart item
     */
    public CartItemDTO addToCart(AddToCartRequest request) {
        Long userId = getCurrentUserId();
        CartItem item = cartTransaction.addToCart(userId, request);
        return mapToCartItemDTO(item);
    }

    /**
     * Updates the quantity of an existing cart item owned by the authenticated user.
     *
     * @param cartItemId id of the cart item to update
     * @param request    validated update payload
     * @return {@link CartItemDTO} of the updated cart item
     */
    public CartItemDTO updateCartItem(Long cartItemId, UpdateCartRequest request) {
        Long userId = getCurrentUserId();
        CartItem item = cartTransaction.updateCartItem(cartItemId, userId, request);
        return mapToCartItemDTO(item);
    }

    /**
     * Removes a single item from the authenticated user's cart.
     *
     * @param cartItemId id of the cart item to remove
     */
    public void removeCartItem(Long cartItemId) {
        Long userId = getCurrentUserId();
        cartTransaction.removeCartItem(cartItemId, userId);
    }

    /**
     * Clears all items from the authenticated user's cart.
     */
    public void clearCart() {
        Long userId = getCurrentUserId();
        cartTransaction.clearCart(userId);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Extracts the authenticated user's id from the security context.
     *
     * @return the current user's database id
     * @throws ResourceNotFoundException if no user exists for the authenticated email
     */
    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }

    /**
     * Maps a {@link CartItem} entity to a {@link CartItemDTO}.
     *
     * <p>{@code subtotal} is computed as {@code unitPrice × quantity}.</p>
     *
     * @param item the cart item entity
     * @return the populated response DTO
     */
    private CartItemDTO mapToCartItemDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setBookId(item.getBook().getId());
        dto.setBookTitle(item.getBook().getTitle());
        dto.setBookAuthor(item.getBook().getAuthor());
        dto.setBookImageUrl(item.getBook().getImageUrl());
        dto.setUnitPrice(item.getBook().getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return dto;
    }
}
