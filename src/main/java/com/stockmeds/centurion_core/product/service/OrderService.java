package com.stockmeds.centurion_core.product.service;

import com.stockmeds.centurion_core.config.CenturionThreadLocal;
import com.stockmeds.centurion_core.exception.CustomException;
import com.stockmeds.centurion_core.enums.ErrorCode;
import com.stockmeds.centurion_core.enums.PaymentStatus;
import com.stockmeds.centurion_core.enums.Status;
import com.stockmeds.centurion_core.product.entity.CartItem;
import com.stockmeds.centurion_core.product.entity.Order;
import com.stockmeds.centurion_core.product.entity.OrderItem;
import com.stockmeds.centurion_core.product.entity.ProductEntity;
import com.stockmeds.centurion_core.product.record.OrderResponse;
import com.stockmeds.centurion_core.product.record.PlaceOrderRequest;
import com.stockmeds.centurion_core.product.repository.CartItemRepository;
import com.stockmeds.centurion_core.product.repository.OrderItemRepository;
import com.stockmeds.centurion_core.product.repository.OrderRepository;
import com.stockmeds.centurion_core.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            CartItemRepository cartItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        // Create new order
        Order order = new Order();
        order.setAccountId(accountId);
        order.setOrderNumber(generateOrderNumber());
        order.setShippingAddress(request.shippingAddress());
        order.setBillingAddress(request.billingAddress());
        order.setPaymentMethod(request.paymentMethod());
        order.setNotes(request.notes());
        order.setStatus(Status.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        // Save order first to get ID
        order = orderRepository.save(order);

        // Calculate totals
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        // Create order items
        for (PlaceOrderRequest.OrderItemRequest itemRequest : request.items()) {
            ProductEntity product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_FOUND));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(product.getPrice());

            // Calculate tax for this item
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            BigDecimal itemTax = BigDecimal.ZERO;

            if (product.getGstPercentage() != null) {
                itemTax = itemTotal.multiply(product.getGstPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            orderItem.setTaxAmount(itemTax);
            orderItemRepository.save(orderItem);

            totalAmount = totalAmount.add(itemTotal);
            totalTax = totalTax.add(itemTax);
        }

        // Update order totals
        order.setTotalAmount(totalAmount);
        order.setTaxAmount(totalTax);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFinalAmount(totalAmount.add(totalTax));

        order = orderRepository.save(order);

        //TODO: is this required?
        // Clear account's cart after successful order
        clearAccountCart(accountId);

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse placeOrderFromCart(PlaceOrderRequest request) {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();

        // Get cart items directly by accountId (no Cart entity needed)
        List<CartItem> cartItems = cartItemRepository.findByAccountId(accountId);

        if (cartItems.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.CART_EMPTY);
        }

        // Create new order
        Order order = new Order();
        order.setAccountId(accountId);
        order.setOrderNumber(generateOrderNumber());
        order.setShippingAddress(request.shippingAddress());
        order.setBillingAddress(request.billingAddress());
        order.setPaymentMethod(request.paymentMethod());
        order.setNotes(request.notes());
        order.setStatus(Status.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        // Save order first to get ID
        order = orderRepository.save(order);

        // Calculate totals
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        // Create order items from cart items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());

            // Calculate tax for this item
            BigDecimal itemTotal = cartItem.getTotalPrice();
            BigDecimal itemTax = BigDecimal.ZERO;

            if (cartItem.getProduct().getGstPercentage() != null) {
                itemTax = itemTotal.multiply(cartItem.getProduct().getGstPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            orderItem.setTaxAmount(itemTax);
            orderItemRepository.save(orderItem);

            totalAmount = totalAmount.add(itemTotal);
            totalTax = totalTax.add(itemTax);
        }

        // Update order totals
        order.setTotalAmount(totalAmount);
        order.setTaxAmount(totalTax);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFinalAmount(totalAmount.add(totalTax));

        order = orderRepository.save(order);

        // Clear account's cart after successful order
        clearAccountCart(accountId);

        return mapToOrderResponse(order);
    }

    //TODO: paginate this API
    public List<OrderResponse> getAccountOrders() {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        List<Order> orders = orderRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    public OrderResponse getOrder(Long orderId) {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        Order order = orderRepository.findByIdAndAccountId(orderId, accountId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.ORDER_NOT_FOUND));

        return mapToOrderResponse(order);
    }

    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.ORDER_NOT_FOUND));

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Status status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.ORDER_NOT_FOUND));

        order.setStatus(status);
        order = orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.ORDER_NOT_FOUND));

        order.setPaymentStatus(paymentStatus);

        if (PaymentStatus.PAID.equals(paymentStatus)) {
            order.setStatus(Status.CONFIRMED);
        }

        order = orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    private void clearAccountCart(Integer accountId) {
        // Directly delete cart items by accountId (no Cart entity needed)
        cartItemRepository.deleteByAccountId(accountId);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + uuid;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

        List<OrderResponse.OrderItemResponse> itemResponses = orderItems.stream()
                .map(item -> new OrderResponse.OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getBrand(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice(),
                        item.getDiscountAmount(),
                        item.getTaxAmount(),
                        item.getProduct().getPackaging(),
                        item.getProduct().getPrescriptionRequired()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getAccountId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getTaxAmount(),
                order.getFinalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                order.getBillingAddress(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getNotes(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
