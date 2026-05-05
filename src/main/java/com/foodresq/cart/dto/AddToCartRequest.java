package com.foodresq.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequest {

    private Long listingId;
    private Integer quantity;
}