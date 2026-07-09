package com.bookstore.transaction;

import com.bookstore.dao.BookDAO;
import com.bookstore.dao.CartItemDAO;
import com.bookstore.dao.OrderDAO;
import com.bookstore.dao.OrderItemDAO;
import com.bookstore.dao.UserDAO;
import com.bookstore.dto.order.PlaceOrderRequest;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.exception.UnauthorizedException;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.OrderStatus;
import com.bookstore.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Transactional unit of work for {@link Order} operations.
 *
 * <p>All state-changing methods are fully atomic: stock adjustments, order
 * persistence, and cart clearing happen together or not at all.</p>
 */
@Component
@Transactional
public class OrderTransaction {

    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final CartItemDAO cartItemDAO;
    private final BookDAO bookDAO;
    private final UserDAO userDAO;

    public OrderTransaction(OrderDAO orderDAO,
                            OrderItemDAO orderItemDAO,
                            CartItemDAO cartItemDAO,
                            BookDAO bookDAO,
                            UserDAO userDAO) {
        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
        this.cartItemDAO = cartItemDAO;
        this.bookDAO = bookDAO;
        this.userDAO = userDAO;
    }

    /**
     * Places a new order by snapshotting the user's current cart into {@link OrderItem}s,
     * decrementing stock, saving the order, and clearing the cart — atomically.
     *
     * @param userId  id of the authenticated user
     * @param request validated request containing the shipping address
     * @return the persisted {@link Order}
     * @throws BadRequestException       if the cart is empty
     * @throws InsufficientStockException if any book has insufficient stock
     */
    @Transactional
    public Order placeOrder(Long userId, PlaceOrderRequest request) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<CartItem> cartItems = cartItemDAO.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // Validate stock for every item before touching anything
        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            if (book.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        book.getTitle(), cartItem.getQuantity(), book.getStockQuantity());
            }
        }

        // Build OrderItems and compute total
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            BigDecimal unitPrice = book.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(lineTotal);

            orderItems.add(OrderItem.builder()
                    .book(book)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(unitPrice)
                    .build());
        }

        // Build and save the Order (cascade saves OrderItems)
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .shippingAddress(request.getShippingAddress())
                .orderItems(orderItems)
                .build();

        // Link each OrderItem back to the Order
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }

        Order savedOrder = orderDAO.save(order);

        // Decrement stock for each book
        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            book.setStockQuantity(book.getStockQuantity() - cartItem.getQuantity());
            bookDAO.save(book);
        }

        // Clear the cart
        cartItemDAO.deleteByUserId(userId);

        return savedOrder;
    }

    /**
     * Returns all orders placed by the given user.
     *
     * @param userId the user's id
     * @return list of {@link Order}s; empty if none exist
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(Long userId) {
        return orderDAO.findByUserId(userId);
    }

    /**
     * Returns a single order, verifying that it belongs to the given user.
     *
     * @param orderId id of the order to retrieve
     * @param userId  id of the authenticated user (ownership check)
     * @return the matching {@link Order}
     * @throws ResourceNotFoundException if no order exists with the given id
     * @throws UnauthorizedException     if the order belongs to a different user
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId, Long userId) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorised to access this order");
        }

        return order;
    }

    /**
     * Updates the status of any order (admin operation — no ownership check).
     *
     * @param orderId   id of the order to update
     * @param newStatus the new {@link OrderStatus} to apply
     * @return the updated {@link Order}
     * @throws ResourceNotFoundException if no order exists with the given id
     */
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(newStatus);
        return orderDAO.save(order);
    }

    /**
     * Cancels a PENDING order owned by the given user, restoring book stock.
     *
     * @param orderId id of the order to cancel
     * @param userId  id of the authenticated user (ownership check)
     * @return the cancelled {@link Order}
     * @throws ResourceNotFoundException if no order exists with the given id
     * @throws UnauthorizedException     if the order belongs to a different user
     * @throws BadRequestException       if the order is not in PENDING status
     */
    public Order cancelOrder(Long orderId, Long userId) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorised to cancel this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be cancelled");
        }

        // Restore stock for each item
        for (OrderItem item : order.getOrderItems()) {
            Book book = item.getBook();
            book.setStockQuantity(book.getStockQuantity() + item.getQuantity());
            bookDAO.save(book);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderDAO.save(order);
    }
}
