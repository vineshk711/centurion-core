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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
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
        Integer accountId = getCurrentAccountId();

        // Create order items from request items
        List<OrderItemData> orderItemsData = request.items().stream()
                .map(this::createOrderItemFromRequest)
                .toList();

        // Validate all items and proceed with order creation
        return validateAndCreateOrder(accountId, request, orderItemsData, "Cannot place order due to the following issues: ");
    }

    @Transactional
    public OrderResponse placeOrderFromCart(PlaceOrderRequest request) {
        Integer accountId = getCurrentAccountId();

        // Get cart items and validate
        List<CartItem> cartItems = cartItemRepository.findByAccountId(accountId);
        if (cartItems.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.CART_EMPTY);
        }

        // Create order items from cart items
        List<OrderItemData> orderItemsData = cartItems.stream()
                .map(this::createOrderItemFromCart)
                .toList();

        // Validate all items and proceed with order creation
        return validateAndCreateOrder(accountId, request, orderItemsData, "Cannot place order from cart due to the following issues: ");
    }


    private OrderResponse validateAndCreateOrder(Integer accountId, PlaceOrderRequest request,
                                                List<OrderItemData> orderItemsData, String errorPrefix) {
        // Check if the initial order items list is empty
        if (orderItemsData == null || orderItemsData.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NO_VALID_ITEMS_TO_ORDER,
                "Order must contain at least one item");
        }

        List<OrderItemData> validOrderItems = new ArrayList<>();
        List<String> stockIssues = new ArrayList<>();

        for (OrderItemData itemData : orderItemsData) {
            try {
                // Validate quantity
                if (itemData.quantity() <= 0) {
                    stockIssues.add(String.format("Product '%s' (ID: %d): Invalid quantity (%d)",
                            itemData.product().getName(), itemData.product().getId(), itemData.quantity()));
                    continue;
                }

                // Validate product price
                if (itemData.product().getPrice() == null || itemData.product().getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    stockIssues.add(String.format("Product '%s' (ID: %d): Invalid price",
                            itemData.product().getName(), itemData.product().getId()));
                    continue;
                }

                // Check stock availability with detailed messaging
                if (itemData.product().getStockQuantity() == null || itemData.product().getStockQuantity() <= 0) {
                    stockIssues.add(String.format("Product '%s' (ID: %d): Out of stock",
                            itemData.product().getName(), itemData.product().getId()));
                    continue;
                }

                if (itemData.product().getStockQuantity() < itemData.quantity()) {
                    stockIssues.add(String.format("Product '%s' (ID: %d): Insufficient stock (Requested: %d, Available: %d)",
                            itemData.product().getName(), itemData.product().getId(),
                            itemData.quantity(), itemData.product().getStockQuantity()));
                    continue;
                }

                // All validations passed, add to valid order items
                validOrderItems.add(itemData);

            } catch (Exception e) {
                log.error("Error processing item for product {}: {}", itemData.product().getId(), e.getMessage());
                stockIssues.add(String.format("Product '%s' (ID: %d): %s",
                        itemData.product().getName(), itemData.product().getId(), e.getMessage()));
            }
        }

        // If there are any stock issues, inform the user and fail the order
        if (!stockIssues.isEmpty()) {
            String errorMessage = errorPrefix + String.join("; ", stockIssues);
            log.warn("Order placement failed for account {}: {}", accountId, errorMessage);
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INSUFFICIENT_STOCK, errorMessage);
        }

        // All items are valid, proceed with order creation
        return createAndProcessOrder(accountId, request, validOrderItems);
    }

    private OrderItemData createOrderItemFromRequest(PlaceOrderRequest.OrderItemRequest itemRequest) {
        ProductEntity product = productRepository.findById(itemRequest.productId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_FOUND));

        // Handle null price gracefully - let validation logic handle the error later
        BigDecimal price = product.getPrice();
        BigDecimal itemTotal = price != null ?
            price.multiply(BigDecimal.valueOf(itemRequest.quantity())) :
            BigDecimal.ZERO;

        return new OrderItemData(product, itemRequest.quantity(), price, itemTotal);
    }

    private OrderItemData createOrderItemFromCart(CartItem cartItem) {
        return new OrderItemData(
                cartItem.getProduct(),
                cartItem.getQuantity(),
                cartItem.getUnitPrice(),
                cartItem.getTotalPrice()
        );
    }

    private OrderResponse createAndProcessOrder(Integer accountId, PlaceOrderRequest request, List<OrderItemData> orderItemsData) {
        // Create new order with all properties set
        Order order = new Order();
        order.setAccountId(accountId);
        order.setOrderNumber(generateOrderNumber());
        order.setShippingAddress(request.shippingAddress());
        order.setBillingAddress(request.billingAddress());
        order.setPaymentMethod(request.paymentMethod());
        order.setNotes(request.notes());
        order.setStatus(Status.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        // Calculate totals, create order items, and update stock in one pass
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<ProductEntity> productsToUpdate = new ArrayList<>();

        for (OrderItemData itemData : orderItemsData) {
            // Final stock check before deduction (double-check for race conditions)
            ProductEntity product = productRepository.findById(itemData.product().getId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_FOUND));

            if (product.getStockQuantity() == null || product.getStockQuantity() < itemData.quantity()) {
                log.error("Stock changed during order processing for product {}: requested {}, available {}",
                        product.getId(), itemData.quantity(), product.getStockQuantity());
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INSUFFICIENT_STOCK);
            }

            // Calculate tax for this item
            BigDecimal itemTax = itemData.product().getGstPercentage() != null
                    ? itemData.itemTotal()
                    .multiply(itemData.product().getGstPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Create order item (will be saved after order is persisted)
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(itemData.product());
            orderItem.setQuantity(itemData.quantity());
            orderItem.setUnitPrice(itemData.unitPrice());
            orderItem.setTaxAmount(itemTax);
            orderItems.add(orderItem);

            // Deduct stock quantity
            product.setStockQuantity(product.getStockQuantity() - itemData.quantity());
            productsToUpdate.add(product);

            log.info("Deducted {} units from product {} stock. New stock: {}",
                    itemData.quantity(), product.getId(), product.getStockQuantity());

            // Accumulate totals
            totalAmount = totalAmount.add(itemData.itemTotal());
            totalTax = totalTax.add(itemTax);
        }

        // Set calculated totals on order before saving
        order.setTotalAmount(totalAmount);
        order.setTaxAmount(totalTax);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFinalAmount(totalAmount.add(totalTax));

        // Save order once with all calculated values
        order = orderRepository.save(order);

        // Bulk save all order items and update product stocks
        orderItemRepository.saveAll(orderItems);
        productRepository.saveAll(productsToUpdate);

        // Clear cart and return response
        clearAccountCart(accountId);
        return mapToOrderResponse(order);
    }
    private void clearAccountCart(Integer accountId) {
        // Directly delete cart items by accountId (no Cart entity needed)
        cartItemRepository.deleteByAccountId(accountId);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("ORD-%s", timestamp);
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

    private Integer getCurrentAccountId() {
        return CenturionThreadLocal.getUserAccountAttributes().getAccountId();
    }

    private record OrderItemData(
            ProductEntity product,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal itemTotal
    ) {}
}
