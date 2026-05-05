package com.foodresq.cart.controller;

import com.foodresq.cart.dto.AddToCartRequest;
import com.foodresq.cart.dto.CartResponse;
import com.foodresq.cart.service.CartService;
import com.foodresq.order.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getMyCart(authentication.getName()));
    }

    @PostMapping("/items")
    public ResponseEntity<Void> addToCart(@RequestBody AddToCartRequest request,
                                          Authentication authentication) {
        cartService.addToCart(request, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId,
                                           Authentication authentication) {
        cartService.removeItem(itemId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(Authentication authentication) {
        return ResponseEntity.ok(cartService.checkout(authentication.getName()));
    }
}