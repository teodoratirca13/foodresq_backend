package com.foodresq.listing.dto;

import com.foodresq.listing.enums.ListingType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Max;

@Getter
@Setter
public class CreateListingRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @DecimalMin("0.0")
    private BigDecimal minimumPrice;

    @Min(0)
    @Max(100)
    private Integer discountPercentage;

    @NotNull
    private LocalDateTime expirationDate;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private ListingType type;

    @NotNull
    private Long ownerId;
}