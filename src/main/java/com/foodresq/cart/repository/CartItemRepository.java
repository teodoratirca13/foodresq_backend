package com.foodresq.cart.repository;

import com.foodresq.cart.Cart;
import com.foodresq.cart.CartItem;
import com.foodresq.listing.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndListing(Cart cart, Listing listing);
}
