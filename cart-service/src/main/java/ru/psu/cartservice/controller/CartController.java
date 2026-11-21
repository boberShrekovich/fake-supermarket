package ru.psu.cartservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.psu.cartservice.dto.AddToCartRequest;
import ru.psu.cartservice.dto.CartItemResponse;
import ru.psu.cartservice.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CartItemResponse>> getCart(@PathVariable("userId") Long userId) {
        return new ResponseEntity<>(cartService.getCart(userId), HttpStatus.OK);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<Void> addToCart(
            @PathVariable("userId") Long userId,
            @RequestBody AddToCartRequest request
    ) {
        cartService.addToCart(userId, request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable("userId") Long userId,
            @PathVariable("productId") Long productId
    ) {
        cartService.removeFromCart(userId, productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable("userId") Long userId) {
        cartService.clearCart(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
