package com.foodresq.cart.service;

import com.foodresq.cart.Cart;
import com.foodresq.cart.CartItem;
import com.foodresq.cart.dto.AddToCartRequest;
import com.foodresq.cart.dto.CartItemResponse;
import com.foodresq.cart.dto.CartResponse;
import com.foodresq.cart.repository.CartItemRepository;
import com.foodresq.cart.repository.CartRepository;
import com.foodresq.listing.entity.Listing;
import com.foodresq.listing.enums.ListingStatus;
import com.foodresq.listing.enums.ListingType;
import com.foodresq.listing.repository.ListingRepository;
import com.foodresq.order.Order;
import com.foodresq.order.OrderItem;
import com.foodresq.order.OrderStatus;
import com.foodresq.order.dto.OrderItemResponse;
import com.foodresq.order.dto.OrderResponse;
import com.foodresq.order.repository.OrderRepository;
import com.foodresq.user.entity.User;
import com.foodresq.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void addToCart(AddToCartRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (listing.getType() != ListingType.SALE) {
            throw new RuntimeException("Only SALE listings can be added to cart");
        }

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new RuntimeException("Listing is not active");
        }

        if (listing.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("You cannot buy your own listing");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        if (request.getQuantity() > listing.getQuantity()) {
            throw new RuntimeException("Not enough quantity available");
        }

        Cart cart = cartRepository.findByUser_Email(email)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .user(user)
                                .items(new ArrayList<>())
                                .build()
                ));

        CartItem item = cartItemRepository.findByCartAndListing(cart, listing)
                .orElse(null);

        if (item == null) {
            item = CartItem.builder()
                    .cart(cart)
                    .listing(listing)
                    .quantity(request.getQuantity())
                    .build();
        } else {
            int newQuantity = item.getQuantity() + request.getQuantity();

            if (newQuantity > listing.getQuantity()) {
                throw new RuntimeException("Not enough quantity available");
            }

            item.setQuantity(newQuantity);
        }

        cartItemRepository.save(item);
    }

    public CartResponse getMyCart(String email) {
        Cart cart = cartRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        return mapCartToResponse(cart);
    }

    @Transactional
    public void removeItem(Long itemId, String email) {
        Cart cart = cartRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("You cannot remove this item");
        }

        cartItemRepository.delete(item);
    }

    @Transactional
    public OrderResponse checkout(String email) {
        Cart cart = cartRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = Order.builder()
                .buyer(cart.getUser())
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Listing listing = cartItem.getListing();

            if (listing.getStatus() != ListingStatus.ACTIVE) {
                throw new RuntimeException("Listing is no longer active: " + listing.getTitle());
            }

            if (cartItem.getQuantity() > listing.getQuantity()) {
                throw new RuntimeException("Not enough quantity for: " + listing.getTitle());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .listing(listing)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(listing.getPrice())
                    .build();

            order.getItems().add(orderItem);

            BigDecimal subtotal = listing.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            total = total.add(subtotal);

            listing.setQuantity(listing.getQuantity() - cartItem.getQuantity());

            if (listing.getQuantity() == 0) {
                listing.setStatus(ListingStatus.COMPLETED);
            }

            listingRepository.save(listing);
        }

        order.setTotalPrice(total);

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return mapOrderToResponse(savedOrder);
    }

    private CartResponse mapCartToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(item -> {
                    BigDecimal subtotal = item.getListing().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

                    return CartItemResponse.builder()
                            .cartItemId(item.getId())
                            .listingId(item.getListing().getId())
                            .title(item.getListing().getTitle())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getListing().getPrice())
                            .subtotal(subtotal)
                            .build();
                })
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .total(total)
                .build();
    }
    @Transactional
    public void clearCart(String email) {
        Cart cart = cartRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private OrderResponse mapOrderToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> OrderItemResponse.builder()
                        .listingId(item.getListing().getId())
                        .title(item.getListing().getTitle())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
