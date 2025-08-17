package com.stockmeds.centurion_core.product.controller;

import com.stockmeds.centurion_core.config.CenturionThreadLocal;
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
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        CartResponse cart = cartService.getCart(accountId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(@RequestBody AddToCartRequest request) {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        CartResponse cart = cartService.addToCart(accountId, request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateCartItem(@RequestBody UpdateCartItemRequest request) {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        CartResponse cart = cartService.updateCartItem(accountId, request);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Integer productId) {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        cartService.removeFromCart(accountId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        Integer accountId = CenturionThreadLocal.getUserAccountAttributes().getAccountId();
        cartService.clearCart(accountId);
        return ResponseEntity.noContent().build();
    }
}
