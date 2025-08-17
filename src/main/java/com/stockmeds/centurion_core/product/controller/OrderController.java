package com.stockmeds.centurion_core.product.controller;

import com.stockmeds.centurion_core.config.CenturionThreadLocal;
import com.stockmeds.centurion_core.enums.PaymentStatus;
import com.stockmeds.centurion_core.enums.Status;
import com.stockmeds.centurion_core.product.record.OrderResponse;
import com.stockmeds.centurion_core.product.record.PlaceOrderRequest;
import com.stockmeds.centurion_core.product.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        OrderResponse order = orderService.placeOrder(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/from-cart")
    public ResponseEntity<OrderResponse> placeOrderFromCart(@RequestBody PlaceOrderRequest request) {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        OrderResponse order = orderService.placeOrderFromCart(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAccountOrders() {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        List<OrderResponse> orders = orderService.getAccountOrders(accountId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        OrderResponse order = orderService.getOrder(accountId, orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/by-number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(@PathVariable String orderNumber) {
        OrderResponse order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Status status) {
        OrderResponse order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<OrderResponse> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus paymentStatus) {
        OrderResponse order = orderService.updatePaymentStatus(orderId, paymentStatus);
        return ResponseEntity.ok(order);
    }
}
