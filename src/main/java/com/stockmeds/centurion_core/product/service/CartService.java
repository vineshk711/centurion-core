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
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public CartResponse getCart() {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        List<CartItem> cartItems = cartItemRepository.findByAccountId(accountId);

        if (cartItems.isEmpty()) {
            return CartResponse.empty(accountId);
        }

        return mapToCartResponse(accountId, cartItems);
    }

    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        if(request.quantity() <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_QUANTITY);
        }

        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();

        // Validate that the product exists
        ProductEntity product = productRepository.findById(request.productId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_FOUND));

        // Try to find existing cart item directly by account and product
        Optional<CartItem> existingCartItem = cartItemRepository
                .findByAccountIdAndProductId(accountId, request.productId());

        // Calculate total quantity needed (existing quantity + new quantity)
        int currentCartQuantity = existingCartItem.map(CartItem::getQuantity).orElse(0);
        int totalQuantityNeeded = currentCartQuantity + request.quantity();

        // Validate stock availability
        if (product.getStockQuantity() == null || product.getStockQuantity() < totalQuantityNeeded) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INSUFFICIENT_STOCK);
        }

        if (existingCartItem.isPresent()) {
            // Update existing cart item
            CartItem item = existingCartItem.get();
            item.setQuantity(totalQuantityNeeded);
            cartItemRepository.save(item);
        } else {
            // Create new cart item directly
            CartItem newItem = new CartItem();
            newItem.setAccountId(accountId);
            newItem.setProduct(product);
            newItem.setQuantity(request.quantity());
            newItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(newItem);
        }

        return getCart();
    }

    @Transactional
    public CartResponse updateCartItem(UpdateCartItemRequest request) {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        CartItem cartItem = cartItemRepository.findById(request.cartItemId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.CART_ITEM_NOT_FOUND));

        // Ensure the cart item belongs to the user
        if (!cartItem.getAccountId().equals(accountId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED_CART_ACCESS);
        }

        if (request.quantity() <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            // Validate stock availability when updating quantity
            ProductEntity product = cartItem.getProduct();
            if (product.getStockQuantity() == null || product.getStockQuantity() < request.quantity()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INSUFFICIENT_STOCK);
            }

            cartItem.setQuantity(request.quantity());
            cartItemRepository.save(cartItem);
        }

        return getCart();
    }

    @Transactional
    public CartResponse removeFromCart(Integer productId) {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();

        // Find and verify the item exists
        Optional<CartItem> existingItem = cartItemRepository
                .findByAccountIdAndProductId(accountId, productId);

        if (existingItem.isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_IN_CART);
        }

        cartItemRepository.deleteByAccountIdAndProductId(accountId, productId);
        return getCart();
    }

    @Transactional
    public void clearCart() {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        cartItemRepository.deleteByAccountId(accountId);
    }

    private CartResponse mapToCartResponse(Integer accountId, List<CartItem> cartItems) {
        List<CartResponse.CartItemResponse> itemResponses = cartItems.stream()
                .map(item -> new CartResponse.CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getBrand(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice(),
                        item.getProduct().getPackaging(),
                        item.getProduct().getPrescriptionRequired()
                ))
                .toList();

        BigDecimal totalAmount = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate timestamps from cart items
        LocalDateTime earliestCreated = cartItems.stream()
                .map(CartItem::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime latestUpdated = cartItems.stream()
                .map(CartItem::getUpdatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new CartResponse(
                accountId,
                itemResponses,
                totalAmount,
                earliestCreated,
                latestUpdated
        );
    }
}
