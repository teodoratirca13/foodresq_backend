package com.foodresq.listing.dto;

import com.foodresq.listing.enums.ListingStatus;
import com.foodresq.listing.enums.ListingType;
import com.foodresq.listing.enums.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    private String ownerName;

    private Long reservedByUserId;
    private String reservedByUserName;
    private LocalDateTime createdAt;
    private ProductCategory category;
}