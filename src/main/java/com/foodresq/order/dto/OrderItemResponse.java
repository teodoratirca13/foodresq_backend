package com.foodresq.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class OrderItemResponse {

    private Long listingId;
    private String title;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}