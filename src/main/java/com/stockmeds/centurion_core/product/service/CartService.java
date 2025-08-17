package com.stockmeds.centurion_core.product.service;

import com.stockmeds.centurion_core.product.entity.Cart;
import com.stockmeds.centurion_core.product.entity.CartItem;
import com.stockmeds.centurion_core.product.entity.ProductEntity;
import com.stockmeds.centurion_core.product.record.AddToCartRequest;
import com.stockmeds.centurion_core.product.record.CartResponse;
import com.stockmeds.centurion_core.product.record.UpdateCartItemRequest;
import com.stockmeds.centurion_core.product.repository.CartItemRepository;
import com.stockmeds.centurion_core.product.repository.CartRepository;
import com.stockmeds.centurion_core.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public CartResponse getCart(Integer accountId) {
        Optional<Cart> cartOpt = cartRepository.findByAccountId(accountId);

        if (cartOpt.isEmpty()) {
            return createEmptyCartResponse(accountId);
        }

        Cart cart = cartOpt.get();
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        return mapToCartResponse(cart, cartItems);
    }

    @Transactional
    public CartResponse addToCart(Integer accountId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(accountId);

        ProductEntity product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.productId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.quantity());
            newItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(newItem);
        }

        return getCart(accountId);
    }

    @Transactional
    public CartResponse updateCartItem(Integer accountId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(request.cartItemId())
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getAccountId().equals(accountId)) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        if (request.quantity() <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(request.quantity());
            // Explicitly recalculate total price to ensure consistency
            if (cartItem.getUnitPrice() != null) {
                cartItem.setTotalPrice(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(request.quantity())));
            }
            cartItemRepository.save(cartItem);
        }

        return getCart(accountId);
    }

    @Transactional
    public CartResponse removeFromCart(Integer accountId, Integer productId) {
        Cart cart = cartRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Verify the item exists before deletion
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId);

        if (existingItem.isEmpty()) {
            throw new RuntimeException("Product not found in cart");
        }

        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);

        return getCart(accountId);
    }

    @Transactional
    public void clearCart(Integer accountId) {
        Cart cart = cartRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteByCartId(cart.getId());
    }

    private Cart getOrCreateCart(Integer accountId) {
        return cartRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setAccountId(accountId);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse createEmptyCartResponse(Integer accountId) {
        return new CartResponse(
                null,
                accountId,
                List.of(),
                BigDecimal.ZERO,
                null,
                null
        );
    }

    private CartResponse mapToCartResponse(Cart cart, List<CartItem> cartItems) {
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

        return new CartResponse(
                cart.getId(),
                cart.getAccountId(),
                itemResponses,
                totalAmount,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
}
