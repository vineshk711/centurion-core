package com.stockmeds.centurion_core.product.controller;

import com.stockmeds.centurion_core.product.record.AddToCartRequest;
import com.stockmeds.centurion_core.product.record.CartResponse;
import com.stockmeds.centurion_core.product.record.UpdateCartItemRequest;
import com.stockmeds.centurion_core.product.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        CartResponse cart = cartService.getCart();
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(@RequestBody AddToCartRequest request) {
        CartResponse cart = cartService.addToCart(request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateCartItem(@RequestBody UpdateCartItemRequest request) {
        CartResponse cart = cartService.updateCartItem(request);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable Integer productId) {
        CartResponse cart = cartService.removeFromCart(productId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
