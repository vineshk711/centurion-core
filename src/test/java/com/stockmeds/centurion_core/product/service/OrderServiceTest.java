package com.stockmeds.centurion_core.product.service;

import com.stockmeds.centurion_core.config.CenturionThreadLocal;
import com.stockmeds.centurion_core.exception.CustomException;
import com.stockmeds.centurion_core.enums.ErrorCode;
import com.stockmeds.centurion_core.enums.PaymentMethod;
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
import com.stockmeds.centurion_core.auth.record.UserAccountAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService - PlaceOrder API Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    private ProductEntity testProduct1;
    private ProductEntity testProduct2;
    private ProductEntity outOfStockProduct;
    private PlaceOrderRequest.OrderItemRequest validOrderItem1;
    private PlaceOrderRequest.OrderItemRequest invalidQuantityItem;
    private PlaceOrderRequest.OrderItemRequest outOfStockItem;
    private PlaceOrderRequest validOrderRequest;
    private Order savedOrder;
    private final Integer ACCOUNT_ID = 12345;

    @BeforeEach
    void setUp() {
        // Setup test products
        testProduct1 = createTestProduct(1, "Paracetamol 500mg", new BigDecimal("10.50"), 100, new BigDecimal("18"));
        testProduct2 = createTestProduct(2, "Aspirin 100mg", new BigDecimal("15.75"), 50, new BigDecimal("12"));
        outOfStockProduct = createTestProduct(3, "Vitamin D3", new BigDecimal("25.00"), 0, new BigDecimal("5"));

        // Setup test order items
        validOrderItem1 = new PlaceOrderRequest.OrderItemRequest(1, 2);
        PlaceOrderRequest.OrderItemRequest validOrderItem2 = new PlaceOrderRequest.OrderItemRequest(2, 3);
        invalidQuantityItem = new PlaceOrderRequest.OrderItemRequest(1, 0);
        outOfStockItem = new PlaceOrderRequest.OrderItemRequest(3, 5);

        // Setup valid order request - correct parameter order
        validOrderRequest = new PlaceOrderRequest(
                "123 Test Street, Test City",
                "456 Billing Street, Billing City",
                PaymentMethod.CREDIT_CARD,
                "Test order notes",
                List.of(validOrderItem1, validOrderItem2)
        );

        // Setup saved order
        savedOrder = createTestOrder();
    }

    @Test
    @DisplayName("Should successfully place order with valid items")
    void testPlaceOrder_Success() {
        // Given
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1));
        when(productRepository.findById(2)).thenReturn(Optional.of(testProduct2));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(createTestOrderItems());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            OrderResponse response = orderService.placeOrder(validOrderRequest);

            // Then
            assertNotNull(response);
            assertEquals(savedOrder.getId(), response.id());
            assertEquals(savedOrder.getOrderNumber(), response.orderNumber());
            assertEquals(Status.PENDING, response.status());
            assertEquals(PaymentStatus.PENDING, response.paymentStatus());

            // Verify stock deduction - productRepository.saveAll() is called once with all products
            verify(productRepository, times(1)).saveAll(anyList());
            verify(orderItemRepository).saveAll(anyList());
            verify(cartItemRepository).deleteByAccountId(ACCOUNT_ID);

            // Verify stock quantities were reduced
            assertEquals(98, testProduct1.getStockQuantity()); // 100 - 2
            assertEquals(47, testProduct2.getStockQuantity()); // 50 - 3
        }
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testPlaceOrder_ProductNotFound() {
        // Given
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrder(validOrderRequest));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());

            // Verify no order was created
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Test
    @DisplayName("Should throw exception when item has invalid quantity")
    void testPlaceOrder_InvalidQuantity() {
        // Given
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1)); // Add this mock setup

        PlaceOrderRequest requestWithInvalidQuantity = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(invalidQuantityItem)
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrder(requestWithInvalidQuantity));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
            assertTrue(exception.getMessage().contains("Invalid quantity (0)"));

            // Verify no order was created
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Test
    @DisplayName("Should throw exception when product is out of stock")
    void testPlaceOrder_OutOfStock() {
        // Given
        when(productRepository.findById(3)).thenReturn(Optional.of(outOfStockProduct));

        PlaceOrderRequest requestWithOutOfStockItem = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(outOfStockItem)
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrder(requestWithOutOfStockItem));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
            assertTrue(exception.getMessage().contains("Out of stock"));

            // Verify no order was created
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Test
    @DisplayName("Should throw exception when requested quantity exceeds available stock")
    void testPlaceOrder_InsufficientStock() {
        // Given
        when(productRepository.findById(2)).thenReturn(Optional.of(testProduct2));

        PlaceOrderRequest.OrderItemRequest excessiveQuantityItem =
                new PlaceOrderRequest.OrderItemRequest(2, 100); // More than available (50)

        PlaceOrderRequest requestWithExcessiveQuantity = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(excessiveQuantityItem)
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrder(requestWithExcessiveQuantity));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
            assertTrue(exception.getMessage().contains("Insufficient stock (Requested: 100, Available: 50)"));

            // Verify no order was created
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Test
    @DisplayName("Should throw exception when product has invalid price")
    void testPlaceOrder_InvalidPrice() {
        // Given
        ProductEntity invalidPriceProduct = createTestProduct(5, "Invalid Price Product", null, 10, new BigDecimal("18"));
        when(productRepository.findById(5)).thenReturn(Optional.of(invalidPriceProduct));

        PlaceOrderRequest.OrderItemRequest invalidPriceItem =
                new PlaceOrderRequest.OrderItemRequest(5, 1);

        PlaceOrderRequest requestWithInvalidPrice = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(invalidPriceItem)
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrder(requestWithInvalidPrice));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
            assertTrue(exception.getMessage().contains("Invalid price"));

            // Verify no order was created
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Test
    @DisplayName("Should handle multiple validation errors in one request")
    void testPlaceOrder_MultipleValidationErrors() {
        // Given
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1));
        when(productRepository.findById(3)).thenReturn(Optional.of(outOfStockProduct));

        PlaceOrderRequest requestWithMultipleErrors = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(invalidQuantityItem, outOfStockItem, validOrderItem1)
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrder(requestWithMultipleErrors));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());

            String errorMessage = exception.getMessage();
            assertTrue(errorMessage.contains("Invalid quantity (0)"));
            assertTrue(errorMessage.contains("Out of stock"));

            // Verify no order was created
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Test
    @DisplayName("Should handle empty order items list")
    void testPlaceOrder_EmptyItemsList() {
        // Given
        PlaceOrderRequest emptyRequest = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                Collections.emptyList()
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrder(emptyRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.NO_VALID_ITEMS_TO_ORDER, exception.getErrorCode());

            // Verify no order was created
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Test
    @DisplayName("Should handle race condition - stock changes during processing")
    void testPlaceOrder_RaceConditionStockChange() {
        // Given
        ProductEntity originalProduct = createTestProduct(1, "Test Product", new BigDecimal("10.00"), 10, new BigDecimal("18"));
        ProductEntity updatedProduct = createTestProduct(1, "Test Product", new BigDecimal("10.00"), 1, new BigDecimal("18"));

        when(productRepository.findById(1))
                .thenReturn(Optional.of(originalProduct))  // First call during validation
                .thenReturn(Optional.of(updatedProduct));  // Second call during processing

        PlaceOrderRequest.OrderItemRequest raceConditionItem =
                new PlaceOrderRequest.OrderItemRequest(1, 5); // More than updated stock

        PlaceOrderRequest raceConditionRequest = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(raceConditionItem)
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrder(raceConditionRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should calculate tax correctly for items with GST")
    void testPlaceOrder_TaxCalculation() {
        // Given
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(createTestOrderItems());

        PlaceOrderRequest singleItemRequest = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(validOrderItem1) // 2 units of product1 at 10.50 each = 21.00, GST 18% = 3.78
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            OrderResponse response = orderService.placeOrder(singleItemRequest);

            // Then
            assertNotNull(response);

            // Verify tax calculation: 21.00 * 0.18 = 3.78
            BigDecimal expectedTax = new BigDecimal("21.00").multiply(new BigDecimal("0.18"));

            // Verify order items were saved with correct tax
            verify(orderItemRepository).saveAll(argThat(orderItems -> {
                List<OrderItem> items = (List<OrderItem>) orderItems;
                return items.size() == 1 &&
                       items.getFirst().getTaxAmount().compareTo(expectedTax) == 0;
            }));
        }
    }

    @Test
    @DisplayName("Should handle product with no GST percentage")
    void testPlaceOrder_NoGSTProduct() {
        // Given
        ProductEntity noGSTProduct = createTestProduct(6, "No GST Product", new BigDecimal("20.00"), 10, null);
        when(productRepository.findById(6)).thenReturn(Optional.of(noGSTProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(createTestOrderItems());

        PlaceOrderRequest.OrderItemRequest noGSTItem = new PlaceOrderRequest.OrderItemRequest(6, 1);
        PlaceOrderRequest noGSTRequest = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(noGSTItem)
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            OrderResponse response = orderService.placeOrder(noGSTRequest);

            // Then
            assertNotNull(response);

            // Verify tax is zero for items without GST
            verify(orderItemRepository).saveAll(argThat(orderItems -> {
                List<OrderItem> items = (List<OrderItem>) orderItems;
                return items.size() == 1 &&
                       items.getFirst().getTaxAmount().compareTo(BigDecimal.ZERO) == 0;
            }));
        }
    }

    @Test
    @DisplayName("Should generate unique order number")
    void testPlaceOrder_OrderNumberGeneration() {
        // Given
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(createTestOrderItems());

        PlaceOrderRequest singleItemRequest = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(validOrderItem1)
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            OrderResponse response = orderService.placeOrder(singleItemRequest);

            // Then
            assertNotNull(response.orderNumber());
            assertTrue(response.orderNumber().startsWith("ORD-"));
            assertTrue(response.orderNumber().length() > 10); // Should contain timestamp and UUID
        }
    }

    // Additional test cases for comprehensive coverage

    @Test
    @DisplayName("Should handle placeOrderFromCart with valid cart items")
    void testPlaceOrderFromCart_Success() {
        // Given
        CartItem cartItem1 = createTestCartItem(testProduct1, 2, new BigDecimal("10.50"));
        CartItem cartItem2 = createTestCartItem(testProduct2, 1, new BigDecimal("15.75"));

        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(List.of(cartItem1, cartItem2));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1));
        when(productRepository.findById(2)).thenReturn(Optional.of(testProduct2));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(createTestOrderItems());

        PlaceOrderRequest cartOrderRequest = new PlaceOrderRequest(
                "123 Cart Street",
                "456 Cart Billing",
                PaymentMethod.CASH,
                "Order from cart",
                Collections.emptyList() // Items come from cart, not request
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            OrderResponse response = orderService.placeOrderFromCart(cartOrderRequest);

            assertNotNull(response);
            verify(cartItemRepository).deleteByAccountId(ACCOUNT_ID);
            verify(productRepository).saveAll(anyList());
            verify(orderItemRepository).saveAll(anyList());
        }
    }

    @Test
    @DisplayName("Should throw exception when cart is empty")
    void testPlaceOrderFromCart_EmptyCart() {
        // Given
        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Collections.emptyList());

        PlaceOrderRequest cartOrderRequest = new PlaceOrderRequest(
                "123 Cart Street",
                "456 Cart Billing",
                PaymentMethod.CASH,
                "Order from cart",
                Collections.emptyList()
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrderFromCart(cartOrderRequest));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.CART_EMPTY, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should handle getAccountOrders successfully")
    void testGetAccountOrders_Success() {
        // Given
        List<Order> mockOrders = List.of(savedOrder, createTestOrder());
        when(orderRepository.findByAccountIdOrderByCreatedAtDesc(ACCOUNT_ID)).thenReturn(mockOrders);
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(createTestOrderItems());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            List<OrderResponse> responses = orderService.getAccountOrders();

            assertNotNull(responses);
            assertEquals(2, responses.size());
            verify(orderRepository).findByAccountIdOrderByCreatedAtDesc(ACCOUNT_ID);
        }
    }

    @Test
    @DisplayName("Should handle getOrder by ID successfully")
    void testGetOrder_Success() {
        // Given
        Long orderId = 1L;
        when(orderRepository.findByIdAndAccountId(orderId, ACCOUNT_ID)).thenReturn(Optional.of(savedOrder));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(createTestOrderItems());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            OrderResponse response = orderService.getOrder(orderId);

            assertNotNull(response);
            assertEquals(orderId, response.id());
            verify(orderRepository).findByIdAndAccountId(orderId, ACCOUNT_ID);
        }
    }

    @Test
    @DisplayName("Should throw exception when order not found by ID")
    void testGetOrder_NotFound() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findByIdAndAccountId(orderId, ACCOUNT_ID)).thenReturn(Optional.empty());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.getOrder(orderId));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should handle getOrderByNumber successfully")
    void testGetOrderByNumber_Success() {
        // Given
        String orderNumber = "ORD-20250819-ABC123";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(savedOrder));
        when(orderItemRepository.findByOrderId(savedOrder.getId())).thenReturn(createTestOrderItems());

        // When & Then
        OrderResponse response = orderService.getOrderByNumber(orderNumber);

        assertNotNull(response);
        assertEquals(orderNumber, response.orderNumber());
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    @DisplayName("Should throw exception when order not found by number")
    void testGetOrderByNumber_NotFound() {
        // Given
        String orderNumber = "ORD-INVALID-123";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.getOrderByNumber(orderNumber));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should update order status successfully")
    void testUpdateOrderStatus_Success() {
        // Given
        Long orderId = 1L;
        Status newStatus = Status.CONFIRMED;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(createTestOrderItems());

        // When
        OrderResponse response = orderService.updateOrderStatus(orderId, newStatus);

        // Then
        assertNotNull(response);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(argThat(order -> order.getStatus() == newStatus));
    }

    @Test
    @DisplayName("Should throw exception when updating status of non-existent order")
    void testUpdateOrderStatus_OrderNotFound() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.updateOrderStatus(orderId, Status.CONFIRMED));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should update payment status successfully")
    void testUpdatePaymentStatus_Success() {
        // Given
        Long orderId = 1L;
        PaymentStatus newPaymentStatus = PaymentStatus.PAID;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(createTestOrderItems());

        // When
        OrderResponse response = orderService.updatePaymentStatus(orderId, newPaymentStatus);

        // Then
        assertNotNull(response);
        verify(orderRepository).save(argThat(order ->
            order.getPaymentStatus() == newPaymentStatus && order.getStatus() == Status.CONFIRMED));
    }

    @Test
    @DisplayName("Should throw exception when updating payment status of non-existent order")
    void testUpdatePaymentStatus_OrderNotFound() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.updatePaymentStatus(orderId, PaymentStatus.PAID));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should handle negative price validation")
    void testPlaceOrder_NegativePrice() {
        // Given
        ProductEntity negativePriceProduct = createTestProduct(7, "Negative Price Product",
                new BigDecimal("-10.00"), 10, new BigDecimal("18"));
        when(productRepository.findById(7)).thenReturn(Optional.of(negativePriceProduct));

        PlaceOrderRequest.OrderItemRequest negativePriceItem = new PlaceOrderRequest.OrderItemRequest(7, 1);
        PlaceOrderRequest requestWithNegativePrice = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(negativePriceItem)
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrder(requestWithNegativePrice));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
            assertTrue(exception.getMessage().contains("Invalid price"));
        }
    }

    @Test
    @DisplayName("Should handle very large quantity orders")
    void testPlaceOrder_VeryLargeQuantity() {
        // Given
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1));

        PlaceOrderRequest.OrderItemRequest largeQuantityItem =
                new PlaceOrderRequest.OrderItemRequest(1, Integer.MAX_VALUE);
        PlaceOrderRequest requestWithLargeQuantity = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(largeQuantityItem)
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrder(requestWithLargeQuantity));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should handle null stock quantity")
    void testPlaceOrder_NullStockQuantity() {
        // Given
        ProductEntity nullStockProduct = createTestProduct(8, "Null Stock Product",
                new BigDecimal("10.00"), null, new BigDecimal("18"));
        when(productRepository.findById(8)).thenReturn(Optional.of(nullStockProduct));

        PlaceOrderRequest.OrderItemRequest nullStockItem = new PlaceOrderRequest.OrderItemRequest(8, 1);
        PlaceOrderRequest requestWithNullStock = new PlaceOrderRequest(
                "123 Test Street",
                "456 Billing Street",
                PaymentMethod.CREDIT_CARD,
                "Test notes",
                List.of(nullStockItem)
        );

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> orderService.placeOrder(requestWithNullStock));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
            assertTrue(exception.getMessage().contains("Out of stock"));
        }
    }

    @Test
    @DisplayName("Should handle database save failure during order creation")
    void testPlaceOrder_DatabaseSaveFailure() {
        // Given
        // Create a fresh product object for this test to avoid interference with other tests
        ProductEntity freshTestProduct = createTestProduct(1, "Paracetamol 500mg", new BigDecimal("10.50"), 100, new BigDecimal("18"));
        when(productRepository.findById(1)).thenReturn(Optional.of(freshTestProduct));
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            PlaceOrderRequest singleItemRequest = new PlaceOrderRequest(
                    "123 Test Street",
                    "456 Billing Street",
                    PaymentMethod.CREDIT_CARD,
                    "Test notes",
                    List.of(validOrderItem1)
            );

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> orderService.placeOrder(singleItemRequest));

            assertTrue(exception.getMessage().contains("Database connection failed"));

            // Verify that no repository saves were called due to the failure
            verify(orderItemRepository, never()).saveAll(anyList());
            verify(productRepository, never()).saveAll(anyList());
            verify(cartItemRepository, never()).deleteByAccountId(ACCOUNT_ID);

            // Note: In a real transactional environment, stock would be rolled back,
            // but with mocks, we can only verify that dependent operations weren't executed
        }
    }

    @Test
    @DisplayName("Should handle CenturionThreadLocal exception")
    void testPlaceOrder_ThreadLocalException() {
        // Given - No mocking of CenturionThreadLocal to simulate missing user context

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.placeOrder(validOrderRequest));

        // This should throw the exception from CenturionThreadLocal.getUserAccountAttributes()
        assertNotNull(exception);
    }

    // Helper method to create cart items
    private CartItem createTestCartItem(ProductEntity product, Integer quantity, BigDecimal unitPrice) {
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setUnitPrice(unitPrice);
        cartItem.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        cartItem.setAccountId(ACCOUNT_ID);
        return cartItem;
    }

    // Helper methods


    private ProductEntity createTestProduct(Integer id, String name, BigDecimal price, Integer stock, BigDecimal gstPercentage) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setGstPercentage(gstPercentage);
        product.setBrand("Test Brand");
        product.setManufacturer("Test Manufacturer");
        product.setPackaging("Test Packaging");
        product.setPrescriptionRequired(false);
        return product;
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setAccountId(ACCOUNT_ID);
        order.setOrderNumber("ORD-20250819-ABC123");
        order.setStatus(Status.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setTotalAmount(new BigDecimal("68.25"));
        order.setTaxAmount(new BigDecimal("8.25"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFinalAmount(new BigDecimal("76.50"));
        order.setShippingAddress("123 Test Street, Test City");
        order.setBillingAddress("456 Billing Street, Billing City");
        order.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        order.setNotes("Test order notes");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    private List<OrderItem> createTestOrderItems() {
        List<OrderItem> orderItems = new ArrayList<>();

        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setProduct(testProduct1);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("10.50"));
        item1.setTaxAmount(new BigDecimal("3.78"));
        orderItems.add(item1);

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setProduct(testProduct2);
        item2.setQuantity(3);
        item2.setUnitPrice(new BigDecimal("15.75"));
        item2.setTaxAmount(new BigDecimal("5.67"));
        orderItems.add(item2);

        return orderItems;
    }
}
