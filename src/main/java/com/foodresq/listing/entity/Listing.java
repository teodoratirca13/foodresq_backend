package com.foodresq.listing.entity;

import com.foodresq.listing.enums.ListingStatus;
import com.foodresq.listing.enums.ListingType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private Integer quantity;

    private BigDecimal price;

    private LocalDateTime expirationDate;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    private ListingType type;

    @Enumerated(EnumType.STRING)
    private ListingStatus status;

    private Long ownerId;

    private Long reservedByUserId;

    private LocalDateTime createdAt;
}