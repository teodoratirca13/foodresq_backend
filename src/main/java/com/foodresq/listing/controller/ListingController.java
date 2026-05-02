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
}