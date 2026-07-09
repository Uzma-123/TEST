package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.order.OrderDTO;
import com.bookstore.dto.order.PlaceOrderRequest;
import com.bookstore.dto.order.UpdateOrderStatusRequest;
import com.bookstore.manager.OrderManager;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for order operations.
 *
 * <pre>
 * POST   /api/orders              — place order from current cart (USER)
 * GET    /api/orders              — get current user's orders (USER)
 * GET    /api/orders/{id}         — get order detail (USER, own orders only)
 * PUT    /api/orders/{id}/status  — update order status (ADMIN)
 * DELETE /api/orders/{id}         — cancel order (USER, PENDING only)
 * </pre>
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderManager orderManager;

    public OrderController(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    /**
     * Places a new order from the authenticated user's current cart.
     *
     * @param request validated request containing the shipping address
     * @return {@code 201 Created} with the new {@link OrderDTO}
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {

        OrderDTO dto = orderManager.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", dto));
    }

    /**
     * Returns all orders for the authenticated user.
     *
     * @return {@code 200 OK} with the list of {@link OrderDTO}s
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders() {
        return ResponseEntity.ok(ApiResponse.success(orderManager.getMyOrders()));
    }

    /**
     * Returns a single order belonging to the authenticated user.
     *
     * @param id id of the order to retrieve
     * @return {@code 200 OK} with the matching {@link OrderDTO}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderManager.getOrderById(id)));
    }

    /**
     * Updates the status of any order. Restricted to ADMIN users.
     *
     * @param id      id of the order to update
     * @param request validated request containing the new status
     * @return {@code 200 OK} with the updated {@link OrderDTO}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderDTO dto = orderManager.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", dto));
    }

    /**
     * Cancels a PENDING order belonging to the authenticated user.
     *
     * @param id id of the order to cancel
     * @return {@code 200 OK} with the cancelled {@link OrderDTO}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(@PathVariable Long id) {
        OrderDTO dto = orderManager.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", dto));
    }
}
