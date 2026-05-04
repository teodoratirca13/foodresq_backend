package com.foodresq.listing.controller;

import com.foodresq.listing.dto.CreateListingRequest;
import com.foodresq.listing.dto.ListingResponse;
import com.foodresq.listing.entity.Listing;
import com.foodresq.listing.enums.ListingType;
import com.foodresq.listing.service.ListingService;
import org.springframework.security.core.Authentication;
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
    public ListingResponse createListing(
            @Valid @RequestBody CreateListingRequest request,
            Authentication authentication
    ) {
        return listingService.createListing(request, authentication.getName());
    }

    @GetMapping
    public List<ListingResponse> getListings(@RequestParam(required = false) ListingType type) {
        return listingService.getActiveListings(type);
    }

    @PostMapping("/{id}/reserve")
    public ListingResponse reserveSale(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return listingService.reserveSale(id, authentication.getName());
    }

    @PostMapping("/{id}/claim")
    public ListingResponse claimDonation(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return listingService.claimDonation(id, authentication.getName());
    }

    @PostMapping("/{id}/complete")
    public ListingResponse completeListing(@PathVariable Long id) {
        return listingService.completeListing(id);
    }

    @PostMapping("/{id}/cancel")
    public ListingResponse cancelListing(@PathVariable Long id) {
        return listingService.cancelListing(id);
    }

    @GetMapping("/{id}")
    public ListingResponse getListingById(@PathVariable Long id) {
        return listingService.getListingById(id);
    }
    @GetMapping("/owner/{ownerId}")
    public List<ListingResponse> getListingsByOwner(@PathVariable Long ownerId) {
        return listingService.getListingsByOwner(ownerId);
    }

    @GetMapping("/nearby")
    public List<ListingResponse> getNearbyListings(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5") Double radiusKm
    ) {
        return listingService.getNearbyListings(lat, lng, radiusKm);
    }
    }