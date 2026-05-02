package com.foodresq.listing.service;

import com.foodresq.listing.dto.CreateListingRequest;
import com.foodresq.listing.entity.Listing;
import com.foodresq.listing.enums.ListingStatus;
import com.foodresq.listing.enums.ListingType;
import com.foodresq.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;

    public Listing createListing(CreateListingRequest request) {
        Listing listing = Listing.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .expirationDate(request.getExpirationDate())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .type(request.getType())
                .status(ListingStatus.ACTIVE)
                .ownerId(request.getOwnerId())
                .createdAt(LocalDateTime.now())
                .build();

        return listingRepository.save(listing);
    }

    public List<Listing> getActiveListings(ListingType type) {
        if (type == null) {
            return listingRepository.findByStatus(ListingStatus.ACTIVE);
        }

        return listingRepository.findByStatusAndType(ListingStatus.ACTIVE, type);
    }

    public Listing reserveSale(Long listingId, Long userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing-ul nu exista"));

        if (listing.getType() != ListingType.SALE) {
            throw new RuntimeException("Doar produsele de vanzare pot fi rezervate");
        }

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new RuntimeException("Listing-ul nu este activ");
        }

        listing.setStatus(ListingStatus.RESERVED);
        listing.setReservedByUserId(userId);

        return listingRepository.save(listing);
    }

    public Listing claimDonation(Long listingId, Long ongId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing-ul nu exista"));

        if (listing.getType() != ListingType.DONATION) {
            throw new RuntimeException("Doar donatiile pot fi revendicate");
        }

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new RuntimeException("Donatia nu este activa");
        }

        listing.setStatus(ListingStatus.RESERVED);
        listing.setReservedByUserId(ongId);

        return listingRepository.save(listing);
    }

    public Listing completeListing(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing-ul nu exista"));

        if (listing.getStatus() != ListingStatus.RESERVED) {
            throw new RuntimeException("Doar listing-urile rezervate pot fi finalizate");
        }

        listing.setStatus(ListingStatus.COMPLETED);

        return listingRepository.save(listing);
    }

    public Listing cancelListing(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing-ul nu exista"));

        if (listing.getStatus() == ListingStatus.COMPLETED) {
            throw new RuntimeException("Un listing finalizat nu mai poate fi anulat");
        }

        listing.setStatus(ListingStatus.CANCELLED);

        return listingRepository.save(listing);
    }
    public Listing getListingById(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing-ul nu exista"));
    }
    public List<Listing> getListingsByOwner(Long ownerId) {
        return listingRepository.findByOwnerId(ownerId);
    }

    public List<Listing> getNearbyListings(Double lat, Double lng, Double radiusKm) {
        return listingRepository.findByStatus(ListingStatus.ACTIVE)
                .stream()
                .filter(listing -> distanceKm(lat, lng, listing.getLatitude(), listing.getLongitude()) <= radiusKm)
                .toList();
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadiusKm = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }
}