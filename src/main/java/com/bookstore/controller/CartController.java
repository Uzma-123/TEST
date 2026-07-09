package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.cart.AddToCartRequest;
import com.bookstore.dto.cart.CartItemDTO;
import com.bookstore.dto.cart.UpdateCartRequest;
import com.bookstore.manager.CartManager;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for shopping-cart operations.
 *
 * <p>All endpoints require an authenticated user (enforced by {@code SecurityConfig}).
 * No {@code @PreAuthorize} annotation is needed here.</p>
 *
 * <pre>
 * GET    /api/cart               — get current user's cart items
 * POST   /api/cart               — add a book to cart (or increment quantity)
 * PUT    /api/cart/{cartItemId}  — update quantity of a cart item
 * DELETE /api/cart/{cartItemId}  — remove a single item from cart
 * DELETE /api/cart               — clear entire cart
 * </pre>
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartManager cartManager;

    public CartController(CartManager cartManager) {
        this.cartManager = cartManager;
    }

    /**
     * Returns all cart items for the authenticated user.
     *
     * @return {@code 200 OK} with the list of {@link CartItemDTO}s
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getCart() {
        return ResponseEntity.ok(ApiResponse.success(cartManager.getCart()));
    }

    /**
     * Adds a book to the authenticated user's cart, or increments the quantity
     * if the item already exists.
     *
     * @param request validated add-to-cart payload
     * @return {@code 201 Created} with the saved {@link CartItemDTO}
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(
            @Valid @RequestBody AddToCartRequest request) {

        CartItemDTO dto = cartManager.addToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart", dto));
    }

    /**
     * Updates the quantity of an existing cart item.
     *
     * @param cartItemId id of the cart item to update
     * @param request    validated update payload
     * @return {@code 200 OK} with the updated {@link CartItemDTO}
     */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartRequest request) {

        CartItemDTO dto = cartManager.updateCartItem(cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", dto));
    }

    /**
     * Removes a single item from the authenticated user's cart.
     *
     * @param cartItemId id of the cart item to remove
     * @return {@code 200 OK} with a confirmation message
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(@PathVariable Long cartItemId) {
        cartManager.removeCartItem(cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
    }

    /**
     * Clears all items from the authenticated user's cart.
     *
     * @return {@code 200 OK} with a confirmation message
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartManager.clearCart();
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
