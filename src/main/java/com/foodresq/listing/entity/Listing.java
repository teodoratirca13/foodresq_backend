package com.foodresq.listing.entity;

import com.foodresq.listing.enums.ListingStatus;
import com.foodresq.listing.enums.ListingType;
import com.foodresq.listing.enums.ProductCategory;
import com.foodresq.user.entity.User;

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
    private BigDecimal originalPrice;
    private BigDecimal minimumPrice;
    private Integer discountPercentage;

    private LocalDateTime expirationDate;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    private ListingType type;

    @Enumerated(EnumType.STRING)
    private ListingStatus status;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "reserved_by_user_id")
    private User reservedByUser;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;
}