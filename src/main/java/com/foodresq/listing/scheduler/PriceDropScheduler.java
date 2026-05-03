package com.foodresq.listing.scheduler;

import com.foodresq.listing.entity.Listing;
import com.foodresq.listing.enums.ListingStatus;
import com.foodresq.listing.enums.ListingType;
import com.foodresq.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PriceDropScheduler {

    private final ListingRepository listingRepository;

    @Scheduled(fixedRate = 60000)
    public void applyPriceDrops() {

        List<Listing> activeSales = listingRepository.findByStatusAndType(
                ListingStatus.ACTIVE,
                ListingType.SALE
        );

        LocalDateTime now = LocalDateTime.now();

        for (Listing listing : activeSales) {

            if (listing.getOriginalPrice() == null ||
                    listing.getMinimumPrice() == null ||
                    listing.getCreatedAt() == null ||
                    listing.getExpirationDate() == null) {
                continue;
            }

            LocalDateTime start = listing.getCreatedAt();
            LocalDateTime end = listing.getExpirationDate();

            long totalSeconds = Duration.between(start, end).getSeconds();
            long elapsedSeconds = Duration.between(start, now).getSeconds();

            if (totalSeconds <= 0) {
                listing.setPrice(listing.getMinimumPrice());
                listingRepository.save(listing);
                continue;
            }

            double progress = (double) elapsedSeconds / totalSeconds;

            if (progress < 0) {
                progress = 0;
            }

            if (progress > 1) {
                progress = 1;
            }

            BigDecimal totalDiscount = listing.getOriginalPrice()
                    .subtract(listing.getMinimumPrice());

            BigDecimal currentDiscount = totalDiscount
                    .multiply(BigDecimal.valueOf(progress));

            BigDecimal newPrice = listing.getOriginalPrice()
                    .subtract(currentDiscount)
                    .setScale(2, RoundingMode.HALF_UP);

            if (newPrice.compareTo(listing.getMinimumPrice()) < 0) {
                newPrice = listing.getMinimumPrice();
            }

            if (newPrice.compareTo(listing.getPrice()) != 0) {
                listing.setPrice(newPrice);
                listingRepository.save(listing);
            }
        }

        System.out.println("Proportional price drop checked...");
    }
}