package com.ecommerce.order.service;

import java.util.UUID;

import com.ecommerce.order.dto.AddCartItemRequest;
import com.ecommerce.order.dto.CartResponse;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UpdateCartItemRequest;
import com.ecommerce.order.entity.Cart;
import com.ecommerce.order.entity.CartItem;
import com.ecommerce.order.exception.CartNotFoundException;
import com.ecommerce.order.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cart business logic (LLD §14). Cart lives in the same service as Order per the HLD.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final OrderDownstreamService downstreamService;

    @Transactional
    public CartResponse getOrCreateCart(UUID userId) {
        Cart cart = findOrCreateActiveCart(userId);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(UUID userId, AddCartItemRequest request) {
        Cart cart = findOrCreateActiveCart(userId);
        ProductResponse product = downstreamService.getCatalogProduct(request.getProductId());

        cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + request.getQuantity()),
                        () -> {
                            CartItem item = new CartItem();
                            item.setCart(cart);
                            item.setProductId(product.resolvedId());
                            item.setSku(product.getSku() != null ? product.getSku() : product.resolvedId());
                            item.setQuantity(request.getQuantity());
                            item.setUnitPrice(product.resolvedPrice());
                            cart.getItems().add(item);
                        });

        Cart saved = cartRepository.save(cart);
        log.info("Added product {} to cart {}", request.getProductId(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public CartResponse updateItem(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        Cart cart = requireActiveCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("Cart item not found with id: " + itemId));
        item.setQuantity(request.getQuantity());
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID itemId) {
        Cart cart = requireActiveCart(userId);
        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new CartNotFoundException("Cart item not found with id: " + itemId);
        }
        return toResponse(cartRepository.save(cart));
    }

    private Cart findOrCreateActiveCart(UUID userId) {
        return cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    cart.setStatus(Cart.CartStatus.ACTIVE);
                    return cartRepository.save(cart);
                });
    }

    private Cart requireActiveCart(UUID userId) {
        return cartRepository.findByUserIdAndStatus(userId, Cart.CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("No active cart for user: " + userId));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartResponse.CartItemResponse> items = cart.getItems().stream()
                .map(i -> CartResponse.CartItemResponse.builder()
                        .itemId(i.getId())
                        .productId(i.getProductId())
                        .sku(i.getSku())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .build())
                .toList();
        BigDecimal total = items.stream()
                .map(CartResponse.CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder()
                .cartId(cart.getId())
                .status(cart.getStatus().name())
                .items(items)
                .totalAmount(total)
                .currency("INR")
                .build();
    }
}
