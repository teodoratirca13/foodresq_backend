package com.foodresq.listing.dto;

import com.foodresq.listing.enums.ListingStatus;
import com.foodresq.listing.enums.ListingType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ListingResponse {
    private Long id;
    private String title;
    private String description;
    private Integer quantity;
    private BigDecimal price;
    private LocalDateTime expirationDate;
    private Double latitude;
    private Double longitude;
    private ListingType type;
    private ListingStatus status;
    private Long ownerId;
    private LocalDateTime createdAt;
}