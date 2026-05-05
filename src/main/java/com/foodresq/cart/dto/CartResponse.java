package com.foodresq.cart.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;
    private BigDecimal total;
}