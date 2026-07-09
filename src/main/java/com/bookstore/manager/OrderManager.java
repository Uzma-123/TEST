package com.bookstore.manager;

import com.bookstore.dao.UserDAO;
import com.bookstore.dto.order.OrderDTO;
import com.bookstore.dto.order.OrderItemDTO;
import com.bookstore.dto.order.PlaceOrderRequest;
import com.bookstore.dto.order.UpdateOrderStatusRequest;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.User;
import com.bookstore.transaction.OrderTransaction;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates order business logic for the authenticated user.
 *
 * <p>Resolves the current user from {@link SecurityContextHolder} and delegates
 * all transactional work to {@link OrderTransaction}.</p>
 */
@Service
public class OrderManager {

    private final OrderTransaction orderTransaction;
    private final UserDAO userDAO;

    public OrderManager(OrderTransaction orderTransaction, UserDAO userDAO) {
        this.orderTransaction = orderTransaction;
        this.userDAO = userDAO;
    }

    /**
     * Places a new order from the authenticated user's current cart.
     *
     * @param request validated request containing the shipping address
     * @return {@link OrderDTO} of the newly created order
     */
    public OrderDTO placeOrder(PlaceOrderRequest request) {
        Long userId = getCurrentUserId();
        Order order = orderTransaction.placeOrder(userId, request);
        return mapToOrderDTO(order);
    }

    /**
     * Returns all orders placed by the authenticated user.
     *
     * @return list of {@link OrderDTO}s; empty if none exist
     */
    public List<OrderDTO> getMyOrders() {
        Long userId = getCurrentUserId();
        return orderTransaction.getOrdersByUser(userId).stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single order belonging to the authenticated user.
     *
     * @param orderId id of the order to retrieve
     * @return the matching {@link OrderDTO}
     */
    public OrderDTO getOrderById(Long orderId) {
        Long userId = getCurrentUserId();
        Order order = orderTransaction.getOrderById(orderId, userId);
        return mapToOrderDTO(order);
    }

    /**
     * Updates the status of any order (admin-only operation).
     *
     * @param orderId id of the order to update
     * @param request validated request containing the new status
     * @return the updated {@link OrderDTO}
     */
    public OrderDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderTransaction.updateOrderStatus(orderId, request.getStatus());
        return mapToOrderDTO(order);
    }

    /**
     * Cancels a PENDING order belonging to the authenticated user, restoring stock.
     *
     * @param orderId id of the order to cancel
     * @return the cancelled {@link OrderDTO}
     */
    public OrderDTO cancelOrder(Long orderId) {
        Long userId = getCurrentUserId();
        Order order = orderTransaction.cancelOrder(orderId, userId);
        return mapToOrderDTO(order);
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
     * Maps an {@link Order} entity to an {@link OrderDTO}, including all line items.
     *
     * @param order the order entity
     * @return the populated response DTO
     */
    private OrderDTO mapToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setItems(order.getOrderItems().stream()
                .map(this::mapToOrderItemDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    /**
     * Maps an {@link OrderItem} entity to an {@link OrderItemDTO}.
     *
     * <p>{@code subtotal} is computed as {@code unitPrice × quantity}.</p>
     *
     * @param item the order item entity
     * @return the populated response DTO
     */
    private OrderItemDTO mapToOrderItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setBookId(item.getBook().getId());
        dto.setBookTitle(item.getBook().getTitle());
        dto.setBookAuthor(item.getBook().getAuthor());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return dto;
    }
}
