package com.bookstore.transaction;

import com.bookstore.dao.BookDAO;
import com.bookstore.dao.CartItemDAO;
import com.bookstore.dao.UserDAO;
import com.bookstore.dto.cart.AddToCartRequest;
import com.bookstore.dto.cart.UpdateCartRequest;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.exception.UnauthorizedException;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import com.bookstore.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Transactional unit of work for {@link CartItem} operations.
 *
 * <p>All public methods participate in a transaction. Stock validation is
 * enforced here before any write to keep the invariant in one place.</p>
 */
@Component
@Transactional
public class CartTransaction {

    private final CartItemDAO cartItemDAO;
    private final BookDAO bookDAO;
    private final UserDAO userDAO;

    public CartTransaction(CartItemDAO cartItemDAO, BookDAO bookDAO, UserDAO userDAO) {
        this.cartItemDAO = cartItemDAO;
        this.bookDAO = bookDAO;
        this.userDAO = userDAO;
    }

    /**
     * Returns all cart items belonging to the given user.
     *
     * @param userId the user's id
     * @return list of {@link CartItem}s; empty if the cart is empty
     */
    @Transactional(readOnly = true)
    public List<CartItem> getCartByUser(Long userId) {
        return cartItemDAO.findByUserId(userId);
    }

    /**
     * Adds a book to the user's cart, or increments the quantity if the item
     * already exists.
     *
     * <ol>
     *   <li>Loads the {@link Book}; throws {@link ResourceNotFoundException} if absent.</li>
     *   <li>Validates available stock; throws {@link InsufficientStockException} if low.</li>
     *   <li>Upserts the cart item (create or increment quantity).</li>
     * </ol>
     *
     * @param userId  id of the authenticated user
     * @param request validated add-to-cart payload
     * @return the saved (new or updated) {@link CartItem}
     */
    public CartItem addToCart(Long userId, AddToCartRequest request) {
        Book book = bookDAO.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        User user = userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<CartItem> existing = cartItemDAO.findByUserIdAndBookId(userId, request.getBookId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            validateStock(book, newQuantity);
            item.setQuantity(newQuantity);
            return cartItemDAO.save(item);
        }

        validateStock(book, request.getQuantity());

        CartItem newItem = CartItem.builder()
                .user(user)
                .book(book)
                .quantity(request.getQuantity())
                .build();

        return cartItemDAO.save(newItem);
    }

    /**
     * Updates the quantity of an existing cart item.
     *
     * <ol>
     *   <li>Loads the {@link CartItem}; throws {@link ResourceNotFoundException} if absent.</li>
     *   <li>Verifies ownership; throws {@link UnauthorizedException} if the item belongs to another user.</li>
     *   <li>Validates available stock; throws {@link InsufficientStockException} if low.</li>
     *   <li>Saves and returns the updated item.</li>
     * </ol>
     *
     * @param cartItemId id of the cart item to update
     * @param userId     id of the authenticated user (ownership check)
     * @param request    validated update payload
     * @return the updated {@link CartItem}
     */
    public CartItem updateCartItem(Long cartItemId, Long userId, UpdateCartRequest request) {
        CartItem item = cartItemDAO.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!item.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorised to modify this cart item");
        }

        Book book = item.getBook();
        validateStock(book, request.getQuantity());

        item.setQuantity(request.getQuantity());
        return cartItemDAO.save(item);
    }

    /**
     * Removes a single item from the cart.
     *
     * @param cartItemId id of the cart item to remove
     * @param userId     id of the authenticated user (ownership check)
     * @throws ResourceNotFoundException if the item does not exist
     * @throws UnauthorizedException     if the item belongs to another user
     */
    public void removeCartItem(Long cartItemId, Long userId) {
        CartItem item = cartItemDAO.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!item.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorised to remove this cart item");
        }

        cartItemDAO.delete(item);
    }

    /**
     * Removes all cart items for the given user.
     *
     * @param userId the user whose cart should be cleared
     */
    public void clearCart(Long userId) {
        cartItemDAO.deleteByUserId(userId);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private void validateStock(Book book, int requestedQuantity) {
        if (book.getStockQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                    book.getTitle(), requestedQuantity, book.getStockQuantity());
        }
    }
}
