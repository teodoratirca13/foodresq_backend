package com.foodresq.listing.repository;

import com.foodresq.listing.entity.Listing;
import com.foodresq.listing.enums.ListingStatus;
import com.foodresq.listing.enums.ListingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByStatus(ListingStatus status);

    List<Listing> findByStatusAndType(ListingStatus status, ListingType type);

    List<Listing> findByOwnerId(Long ownerId);

    List<Listing> findByStatusAndExpirationDateBefore(
            ListingStatus status,
            LocalDateTime time
    );

}