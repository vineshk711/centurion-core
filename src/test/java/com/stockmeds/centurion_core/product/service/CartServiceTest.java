package com.stockmeds.centurion_core.product.service;

import com.stockmeds.centurion_core.config.CenturionThreadLocal;
import com.stockmeds.centurion_core.exception.CustomException;
import com.stockmeds.centurion_core.enums.ErrorCode;
import com.stockmeds.centurion_core.product.entity.CartItem;
import com.stockmeds.centurion_core.product.entity.ProductEntity;
import com.stockmeds.centurion_core.product.record.AddToCartRequest;
import com.stockmeds.centurion_core.product.record.CartResponse;
import com.stockmeds.centurion_core.product.record.UpdateCartItemRequest;
import com.stockmeds.centurion_core.product.repository.CartItemRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private ProductEntity testProduct1;
    private ProductEntity testProduct2;
    private ProductEntity outOfStockProduct;
    private CartItem existingCartItem;
    private final Integer ACCOUNT_ID = 12345;

    @BeforeEach
    void setUp() {
        // Setup test products
        testProduct1 = createTestProduct(1, "Paracetamol 500mg", new BigDecimal("10.50"), 100);
        testProduct2 = createTestProduct(2, "Aspirin 100mg", new BigDecimal("15.75"), 50);
        outOfStockProduct = createTestProduct(3, "Vitamin D3", new BigDecimal("25.00"), 0);

        // Setup existing cart item
        existingCartItem = createTestCartItem(1L, testProduct1, 2, new BigDecimal("10.50"));
    }

    // ========== getCart() Tests ==========

    @Test
    @DisplayName("Should return empty cart when no items exist")
    void testGetCart_EmptyCart() {
        // Given
        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Collections.emptyList());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CartResponse response = cartService.getCart();

            assertNotNull(response);
            assertEquals(ACCOUNT_ID, response.accountId());
            assertTrue(response.items().isEmpty());
            assertEquals(BigDecimal.ZERO, response.totalAmount());
            assertNull(response.createdAt());
            assertNull(response.updatedAt());
        }
    }

    @Test
    @DisplayName("Should return cart with items when items exist")
    void testGetCart_WithItems() {
        // Given
        CartItem cartItem1 = createTestCartItem(1L, testProduct1, 2, new BigDecimal("10.50"));
        CartItem cartItem2 = createTestCartItem(2L, testProduct2, 3, new BigDecimal("15.75"));
        List<CartItem> cartItems = List.of(cartItem1, cartItem2);

        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(cartItems);

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CartResponse response = cartService.getCart();

            assertNotNull(response);
            assertEquals(ACCOUNT_ID, response.accountId());
            assertEquals(2, response.items().size());

            // Verify total amount calculation: (2 * 10.50) + (3 * 15.75) = 21.00 + 47.25 = 68.25
            assertEquals(new BigDecimal("68.25"), response.totalAmount());

            // Verify cart item details
            CartResponse.CartItemResponse item1 = response.items().getFirst();
            assertEquals(testProduct1.getId(), item1.productId());
            assertEquals(testProduct1.getName(), item1.productName());
            assertEquals(2, item1.quantity());
            assertEquals(new BigDecimal("21.00"), item1.totalPrice());
        }
    }

    @Test
    @DisplayName("Should handle CenturionThreadLocal exception in getCart")
    void testGetCart_ThreadLocalException() {
        // Given - No mocking of CenturionThreadLocal to simulate missing user context

        // When & Then
        assertThrows(CustomException.class, () -> cartService.getCart());
    }

    // ========== addToCart() Tests ==========

    @Test
    @DisplayName("Should successfully add new product to cart")
    void testAddToCart_NewProduct_Success() {
        // Given
        AddToCartRequest request = new AddToCartRequest(1, 3);
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByAccountIdAndProductId(ACCOUNT_ID, 1)).thenReturn(Optional.empty());
        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(List.of(existingCartItem));

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CartResponse response = cartService.addToCart(request);

            assertNotNull(response);
            verify(cartItemRepository).save(argThat(cartItem ->
                cartItem.getAccountId().equals(ACCOUNT_ID) &&
                cartItem.getProduct().getId().equals(1) &&
                cartItem.getQuantity().equals(3) &&
                cartItem.getUnitPrice().equals(testProduct1.getPrice())
            ));
        }
    }

    @Test
    @DisplayName("Should successfully update existing cart item quantity")
    void testAddToCart_ExistingProduct_Success() {
        // Given
        AddToCartRequest request = new AddToCartRequest(1, 3);
        CartItem existingItem = createTestCartItem(1L, testProduct1, 2, new BigDecimal("10.50"));

        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByAccountIdAndProductId(ACCOUNT_ID, 1)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(List.of(existingItem));

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CartResponse response = cartService.addToCart(request);

            assertNotNull(response);
            // Verify quantity was updated to existing (2) + new (3) = 5
            assertEquals(5, existingItem.getQuantity());
            verify(cartItemRepository).save(existingItem);
        }
    }

    @Test
    @DisplayName("Should throw exception when adding invalid quantity")
    void testAddToCart_InvalidQuantity() {
        // Given
        AddToCartRequest request = new AddToCartRequest(1, 0);

        // When & Then
        // No need to mock CenturionThreadLocal since the validation happens before it's called
        CustomException exception = assertThrows(CustomException.class,
                () -> cartService.addToCart(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(ErrorCode.INVALID_QUANTITY, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when adding negative quantity")
    void testAddToCart_NegativeQuantity() {
        // Given
        AddToCartRequest request = new AddToCartRequest(1, -5);

        // When & Then
        // No need to mock CenturionThreadLocal since the validation happens before it's called
        CustomException exception = assertThrows(CustomException.class,
                () -> cartService.addToCart(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(ErrorCode.INVALID_QUANTITY, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testAddToCart_ProductNotFound() {
        // Given
        AddToCartRequest request = new AddToCartRequest(999, 2);
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.addToCart(request));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock for new item")
    void testAddToCart_InsufficientStock_NewItem() {
        // Given
        AddToCartRequest request = new AddToCartRequest(1, 150); // More than available stock (100)
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByAccountIdAndProductId(ACCOUNT_ID, 1)).thenReturn(Optional.empty());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.addToCart(request));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock for existing item update")
    void testAddToCart_InsufficientStock_ExistingItem() {
        // Given
        AddToCartRequest request = new AddToCartRequest(1, 99); // Existing 2 + new 99 = 101 > 100 available
        CartItem existingItem = createTestCartItem(1L, testProduct1, 2, new BigDecimal("10.50"));

        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByAccountIdAndProductId(ACCOUNT_ID, 1)).thenReturn(Optional.of(existingItem));

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.addToCart(request));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should throw exception when adding to cart for out of stock product")
    void testAddToCart_OutOfStockProduct() {
        // Given
        AddToCartRequest request = new AddToCartRequest(3, 1);
        when(productRepository.findById(3)).thenReturn(Optional.of(outOfStockProduct));
        when(cartItemRepository.findByAccountIdAndProductId(ACCOUNT_ID, 3)).thenReturn(Optional.empty());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.addToCart(request));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should handle product with null stock quantity")
    void testAddToCart_NullStockQuantity() {
        // Given
        ProductEntity nullStockProduct = createTestProduct(4, "Null Stock Product", new BigDecimal("20.00"), null);
        AddToCartRequest request = new AddToCartRequest(4, 1);

        when(productRepository.findById(4)).thenReturn(Optional.of(nullStockProduct));
        when(cartItemRepository.findByAccountIdAndProductId(ACCOUNT_ID, 4)).thenReturn(Optional.empty());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.addToCart(request));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
        }
    }

    // ========== updateCartItem() Tests ==========

    @Test
    @DisplayName("Should successfully update cart item quantity")
    void testUpdateCartItem_Success() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest(1L, 5);
        CartItem cartItem = createTestCartItem(1L, testProduct1, 3, new BigDecimal("10.50"));
        cartItem.setAccountId(ACCOUNT_ID);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(List.of(cartItem));

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CartResponse response = cartService.updateCartItem(request);

            assertNotNull(response);
            assertEquals(5, cartItem.getQuantity());
            verify(cartItemRepository).save(cartItem);
        }
    }

    @Test
    @DisplayName("Should delete cart item when updating quantity to zero")
    void testUpdateCartItem_DeleteWhenZeroQuantity() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest(1L, 0);
        CartItem cartItem = createTestCartItem(1L, testProduct1, 3, new BigDecimal("10.50"));
        cartItem.setAccountId(ACCOUNT_ID);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Collections.emptyList());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CartResponse response = cartService.updateCartItem(request);

            assertNotNull(response);
            verify(cartItemRepository).delete(cartItem);
            verify(cartItemRepository, never()).save(cartItem);
        }
    }

    @Test
    @DisplayName("Should delete cart item when updating quantity to negative")
    void testUpdateCartItem_DeleteWhenNegativeQuantity() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest(1L, -2);
        CartItem cartItem = createTestCartItem(1L, testProduct1, 3, new BigDecimal("10.50"));
        cartItem.setAccountId(ACCOUNT_ID);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Collections.emptyList());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CartResponse response = cartService.updateCartItem(request);

            assertNotNull(response);
            verify(cartItemRepository).delete(cartItem);
        }
    }

    @Test
    @DisplayName("Should throw exception when cart item not found")
    void testUpdateCartItem_NotFound() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest(999L, 2);
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.updateCartItem(request));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals(ErrorCode.CART_ITEM_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should throw exception when updating cart item belonging to different user")
    void testUpdateCartItem_UnauthorizedAccess() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest(1L, 2);
        CartItem cartItem = createTestCartItem(1L, testProduct1, 3, new BigDecimal("10.50"));
        Integer OTHER_ACCOUNT_ID = 67890;
        cartItem.setAccountId(OTHER_ACCOUNT_ID); // Different account

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.updateCartItem(request));

            assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
            assertEquals(ErrorCode.UNAUTHORIZED_CART_ACCESS, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should throw exception when updating to quantity exceeding stock")
    void testUpdateCartItem_InsufficientStock() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest(1L, 150); // More than available stock (100)
        CartItem cartItem = createTestCartItem(1L, testProduct1, 3, new BigDecimal("10.50"));
        cartItem.setAccountId(ACCOUNT_ID);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.updateCartItem(request));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should handle updating cart item with product having null stock")
    void testUpdateCartItem_ProductNullStock() {
        // Given
        ProductEntity nullStockProduct = createTestProduct(4, "Null Stock Product", new BigDecimal("20.00"), null);
        UpdateCartItemRequest request = new UpdateCartItemRequest(1L, 5);
        CartItem cartItem = createTestCartItem(1L, nullStockProduct, 3, new BigDecimal("20.00"));
        cartItem.setAccountId(ACCOUNT_ID);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.updateCartItem(request));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
        }
    }

    // ========== removeFromCart() Tests ==========

    @Test
    @DisplayName("Should successfully remove product from cart")
    void testRemoveFromCart_Success() {
        // Given
        CartItem cartItem = createTestCartItem(1L, testProduct1, 2, new BigDecimal("10.50"));
        when(cartItemRepository.findByAccountIdAndProductId(ACCOUNT_ID, 1)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Collections.emptyList());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CartResponse response = cartService.removeFromCart(1);

            assertNotNull(response);
            verify(cartItemRepository).deleteByAccountIdAndProductId(ACCOUNT_ID, 1);
        }
    }

    @Test
    @DisplayName("Should throw exception when trying to remove non-existent product")
    void testRemoveFromCart_ProductNotInCart() {
        // Given
        when(cartItemRepository.findByAccountIdAndProductId(ACCOUNT_ID, 999)).thenReturn(Optional.empty());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CustomException exception = assertThrows(CustomException.class,
                    () -> cartService.removeFromCart(999));

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals(ErrorCode.PRODUCT_NOT_IN_CART, exception.getErrorCode());
        }
    }

    // ========== clearCart() Tests ==========

    @Test
    @DisplayName("Should successfully clear cart")
    void testClearCart_Success() {
        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            cartService.clearCart();

            verify(cartItemRepository).deleteByAccountId(ACCOUNT_ID);
        }
    }

    @Test
    @DisplayName("Should handle clearCart with CenturionThreadLocal exception")
    void testClearCart_ThreadLocalException() {
        // Given - No mocking of CenturionThreadLocal to simulate missing user context

        // When & Then
        assertThrows(CustomException.class, () -> cartService.clearCart());
    }

    // ========== Integration & Edge Case Tests ==========

    @Test
    @DisplayName("Should handle very large quantities within stock limits")
    void testAddToCart_LargeQuantityWithinLimits() {
        // Given
        ProductEntity largeStockProduct = createTestProduct(5, "Large Stock Product", new BigDecimal("1.00"), Integer.MAX_VALUE);
        AddToCartRequest request = new AddToCartRequest(5, 1000000);

        when(productRepository.findById(5)).thenReturn(Optional.of(largeStockProduct));
        when(cartItemRepository.findByAccountIdAndProductId(ACCOUNT_ID, 5)).thenReturn(Optional.empty());
        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Collections.emptyList());

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CartResponse response = cartService.addToCart(request);

            assertNotNull(response);
            verify(cartItemRepository).save(any(CartItem.class));
        }
    }

    @Test
    @DisplayName("Should handle cart with mixed items of different prices and quantities")
    void testGetCart_MixedItems() {
        // Given
        CartItem expensiveItem = createTestCartItem(1L, createTestProduct(1, "Expensive Item", new BigDecimal("100.00"), 10), 1, new BigDecimal("100.00"));
        CartItem cheapItem = createTestCartItem(2L, createTestProduct(2, "Cheap Item", new BigDecimal("0.50"), 1000), 20, new BigDecimal("0.50"));
        List<CartItem> cartItems = List.of(expensiveItem, cheapItem);

        when(cartItemRepository.findByAccountId(ACCOUNT_ID)).thenReturn(cartItems);

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            CartResponse response = cartService.getCart();

            assertNotNull(response);
            assertEquals(2, response.items().size());
            // Total: (1 * 100.00) + (20 * 0.50) = 100.00 + 10.00 = 110.00
            assertEquals(new BigDecimal("110.00"), response.totalAmount());
        }
    }

    @Test
    @DisplayName("Should handle database save failure gracefully")
    void testAddToCart_DatabaseSaveFailure() {
        // Given
        AddToCartRequest request = new AddToCartRequest(1, 2);
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByAccountIdAndProductId(ACCOUNT_ID, 1)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        try (MockedStatic<CenturionThreadLocal> mockedStatic = mockStatic(CenturionThreadLocal.class)) {
            UserAccountAttributes mockAttributes = mock(UserAccountAttributes.class);
            when(mockAttributes.getAccountId()).thenReturn(ACCOUNT_ID);
            mockedStatic.when(CenturionThreadLocal::getUserAccountAttributes).thenReturn(mockAttributes);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> cartService.addToCart(request));

            assertTrue(exception.getMessage().contains("Database connection failed"));
        }
    }

    // ========== Helper Methods ==========

    private ProductEntity createTestProduct(Integer id, String name, BigDecimal price, Integer stock) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setBrand("Test Brand");
        product.setManufacturer("Test Manufacturer");
        product.setPackaging("Test Packaging");
        product.setPrescriptionRequired(false);
        return product;
    }

    private CartItem createTestCartItem(Long id, ProductEntity product, Integer quantity, BigDecimal unitPrice) {
        CartItem cartItem = new CartItem();
        cartItem.setId(id);
        cartItem.setAccountId(ACCOUNT_ID);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setUnitPrice(unitPrice);
        cartItem.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        cartItem.setCreatedAt(LocalDateTime.now().minusHours(1));
        cartItem.setUpdatedAt(LocalDateTime.now());
        return cartItem;
    }
}
