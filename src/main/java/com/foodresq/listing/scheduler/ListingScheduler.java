package com.foodresq.listing.scheduler;

import com.foodresq.listing.entity.Listing;
import com.foodresq.listing.enums.ListingStatus;
import com.foodresq.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ListingScheduler {

    private final ListingRepository listingRepository;

    @Scheduled(fixedRate = 60000)
    public void expireListings() {

        List<Listing> expired = listingRepository
                .findByStatusAndExpirationDateBefore(
                        ListingStatus.ACTIVE,
                        LocalDateTime.now()
                );

        for (Listing listing : expired) {
            listing.setStatus(ListingStatus.CANCELLED);
        }

        listingRepository.saveAll(expired);
    }
}