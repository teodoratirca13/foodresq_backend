package com.foodresq.listing.controller;

import com.foodresq.listing.dto.CreateListingRequest;
import com.foodresq.listing.entity.Listing;
import com.foodresq.listing.enums.ListingType;
import com.foodresq.listing.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    public Listing createListing(@Valid @RequestBody CreateListingRequest request) {
        return listingService.createListing(request);
    }

    @GetMapping
    public List<Listing> getListings(@RequestParam(required = false) ListingType type) {
        return listingService.getActiveListings(type);
    }

    @PostMapping("/{id}/reserve")
    public Listing reserveSale(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return listingService.reserveSale(id, userId);
    }

    @PostMapping("/{id}/claim")
    public Listing claimDonation(
            @PathVariable Long id,
            @RequestParam Long ongId
    ) {
        return listingService.claimDonation(id, ongId);
    }

    @PostMapping("/{id}/complete")
    public Listing completeListing(@PathVariable Long id) {
        return listingService.completeListing(id);
    }

    @PostMapping("/{id}/cancel")
    public Listing cancelListing(@PathVariable Long id) {
        return listingService.cancelListing(id);
    }

    @GetMapping("/{id}")
    public Listing getListingById(@PathVariable Long id) {
        return listingService.getListingById(id);
    }
    @GetMapping("/owner/{ownerId}")
    public List<Listing> getListingsByOwner(@PathVariable Long ownerId) {
        return listingService.getListingsByOwner(ownerId);
    }

    @GetMapping("/nearby")
    public List<Listing> getNearbyListings(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5") Double radiusKm
    ) {
        return listingService.getNearbyListings(lat, lng, radiusKm);
    }
}