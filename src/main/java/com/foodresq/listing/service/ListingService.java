package com.foodresq.listing.service;

import com.foodresq.common.exception.BadRequestException;
import com.foodresq.common.exception.ForbiddenActionException;
import com.foodresq.common.exception.ResourceNotFoundException;
import com.foodresq.listing.dto.CreateListingRequest;
import com.foodresq.listing.dto.ListingResponse;
import com.foodresq.listing.entity.Listing;
import com.foodresq.listing.enums.ListingStatus;
import com.foodresq.listing.enums.ListingType;
import com.foodresq.listing.enums.ProductCategory;
import com.foodresq.listing.repository.ListingRepository;
import com.foodresq.user.entity.User;
import com.foodresq.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ListingResponse createListing(CreateListingRequest request, String email) {

        User owner = getUserFromEmail(email);

        if (request.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Expiration date must be in the future");
        }

        if (request.getCategory() == null) {
            throw new BadRequestException("Category is required");
        }

        if (request.getType() == null) {
            throw new BadRequestException("Listing type is required");
        }

        if (request.getType() == ListingType.DONATION) {
            if (request.getPrice() != null && request.getPrice().doubleValue() != 0) {
                throw new BadRequestException("Donations must have a price of 0");
            }

            request.setPrice(java.math.BigDecimal.ZERO);
            request.setMinimumPrice(java.math.BigDecimal.ZERO);
        }

        if (request.getType() == ListingType.SALE) {
            if (request.getPrice() == null || request.getPrice().doubleValue() <= 0) {
                throw new BadRequestException("Sale items must have a price greater than 0");
            }

            if (request.getMinimumPrice().compareTo(request.getPrice()) > 0) {
                throw new BadRequestException("Minimum price cannot be higher than the initial price");
            }
        }

        Listing listing = Listing.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .originalPrice(request.getPrice())
                .minimumPrice(request.getMinimumPrice())
                .discountPercentage(request.getDiscountPercentage())
                .expirationDate(request.getExpirationDate())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .type(request.getType())
                .owner(owner)
                .category(request.getCategory())
                .status(ListingStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        Listing saved = listingRepository.save(listing);
        return mapToResponse(saved);
    }

    public ListingResponse reserveSale(Long listingId, String email) {

        User user = getUserFromEmail(email);
        Listing listing = getListingOrThrow(listingId);

        if (listing.getType() != ListingType.SALE) {
            throw new BadRequestException("Only items for sale can be reserved");
        }

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BadRequestException("The listing is not active");
        }

        if (listing.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenActionException("You cannot reserve your own listing");
        }

        listing.setStatus(ListingStatus.RESERVED);
        listing.setReservedByUser(user);

        Listing saved = listingRepository.save(listing);
        return mapToResponse(saved);
    }

    public ListingResponse claimDonation(Long listingId, String email) {

        User user = getUserFromEmail(email);
        Listing listing = getListingOrThrow(listingId);

        if (listing.getType() != ListingType.DONATION) {
            throw new BadRequestException("Only donation listings can be claimed");
        }

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BadRequestException("The donation is not active");
        }

        if (listing.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenActionException("You cannot claim your own donation");
        }

        listing.setStatus(ListingStatus.RESERVED);
        listing.setReservedByUser(user);

        Listing saved = listingRepository.save(listing);
        return mapToResponse(saved);
    }

    public ListingResponse completeListing(Long listingId, String email) {

        User user = getUserFromEmail(email);
        Listing listing = getListingOrThrow(listingId);

        if (listing.getStatus() != ListingStatus.RESERVED) {
            throw new BadRequestException("Only reserved listings can be completed");
        }

        boolean isOwner = listing.getOwner().getId().equals(user.getId());

        boolean isReservedUser = listing.getReservedByUser() != null
                && listing.getReservedByUser().getId().equals(user.getId());

        if (!isOwner && !isReservedUser) {
            throw new ForbiddenActionException("You are not allowed to complete this listing");
        }

        listing.setStatus(ListingStatus.COMPLETED);

        Listing saved = listingRepository.save(listing);
        return mapToResponse(saved);
    }

    public ListingResponse cancelListing(Long listingId, String email) {

        User user = getUserFromEmail(email);
        Listing listing = getListingOrThrow(listingId);

        if (!listing.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenActionException("Only the owner can cancel this listing");
        }

        if (listing.getStatus() == ListingStatus.COMPLETED) {
            throw new BadRequestException("A completed listing can no longer be cancelled");
        }

        listing.setStatus(ListingStatus.CANCELLED);

        Listing saved = listingRepository.save(listing);
        return mapToResponse(saved);
    }

    public ListingResponse getListingById(Long id) {
        Listing listing = getListingOrThrow(id);
        return mapToResponse(listing);
    }

    public List<ListingResponse> getListingsByOwner(Long ownerId) {
        return listingRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ListingResponse> getNearbyListings(Double lat, Double lng, Double radiusKm) {

        if (lat == null || lng == null || radiusKm == null) {
            throw new BadRequestException("Latitude, longitude and radius are required");
        }

        if (radiusKm <= 0) {
            throw new BadRequestException("Radius must be greater than 0");
        }

        return listingRepository.findByStatus(ListingStatus.ACTIVE)
                .stream()
                .filter(listing -> distanceKm(lat, lng, listing.getLatitude(), listing.getLongitude()) <= radiusKm)
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteListing(Long listingId, String email) {

        User user = getUserFromEmail(email);
        Listing listing = getListingOrThrow(listingId);

        if (!listing.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenActionException("Only owner can delete listing");
        }

        if (listing.getStatus() == ListingStatus.COMPLETED) {
            throw new BadRequestException("Cannot delete completed listing");
        }

        listingRepository.delete(listing);
    }

    public List<ListingResponse> getActiveListings(ListingType type, ProductCategory category) {

        return listingRepository.findByStatus(ListingStatus.ACTIVE)
                .stream()
                .filter(l -> type == null || l.getType() == type)
                .filter(l -> category == null || l.getCategory() == category)
                .map(this::mapToResponse)
                .toList();
    }

    private Listing getListingOrThrow(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("The listing does not exist"));
    }

    private User getUserFromEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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

    private ListingResponse mapToResponse(Listing listing) {
        return ListingResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .quantity(listing.getQuantity())
                .price(listing.getPrice())
                .expirationDate(listing.getExpirationDate())
                .latitude(listing.getLatitude())
                .longitude(listing.getLongitude())
                .category(listing.getCategory())
                .type(listing.getType())
                .status(listing.getStatus())
                .ownerId(listing.getOwner() != null ? listing.getOwner().getId() : null)
                .ownerName(listing.getOwner() != null ? listing.getOwner().getName() : null)
                .reservedByUserId(listing.getReservedByUser() != null ? listing.getReservedByUser().getId() : null)
                .reservedByUserName(listing.getReservedByUser() != null ? listing.getReservedByUser().getName() : null)
                .createdAt(listing.getCreatedAt())
                .build();
    }
}