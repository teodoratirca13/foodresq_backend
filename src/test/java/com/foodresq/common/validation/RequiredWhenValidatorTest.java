package com.foodresq.common.validation;

import com.foodresq.listing.dto.CreateListingRequest;
import com.foodresq.listing.enums.ListingType;
import com.foodresq.listing.enums.ProductCategory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RequiredWhenValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void saleListingWithoutMinimumPrice_shouldFail() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Test Sale");
        request.setQuantity(5);
        request.setPrice(new BigDecimal("10.00"));
        request.setMinimumPrice(null);
        request.setExpirationDate(LocalDateTime.now().plusDays(1));
        request.setLatitude(44.4268);
        request.setLongitude(26.1025);
        request.setType(ListingType.SALE);
        request.setCategory(ProductCategory.PIZZA);

        Set<ConstraintViolation<CreateListingRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("minimumPrice is required for SALE listings")));
    }

    @Test
    void saleListingWithMinimumPrice_shouldPass() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Test Sale");
        request.setQuantity(5);
        request.setPrice(new BigDecimal("10.00"));
        request.setMinimumPrice(new BigDecimal("5.00"));
        request.setExpirationDate(LocalDateTime.now().plusDays(1));
        request.setLatitude(44.4268);
        request.setLongitude(26.1025);
        request.setType(ListingType.SALE);
        request.setCategory(ProductCategory.PIZZA);

        Set<ConstraintViolation<CreateListingRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void donationListingWithoutMinimumPrice_shouldPass() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Test Donation");
        request.setQuantity(5);
        request.setPrice(BigDecimal.ZERO);
        request.setMinimumPrice(null);
        request.setExpirationDate(LocalDateTime.now().plusDays(1));
        request.setLatitude(44.4268);
        request.setLongitude(26.1025);
        request.setType(ListingType.DONATION);
        request.setCategory(ProductCategory.BAKERY);

        Set<ConstraintViolation<CreateListingRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void donationListingWithZeroMinimumPrice_shouldPass() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Test Donation");
        request.setQuantity(5);
        request.setPrice(BigDecimal.ZERO);
        request.setMinimumPrice(BigDecimal.ZERO);
        request.setExpirationDate(LocalDateTime.now().plusDays(1));
        request.setLatitude(44.4268);
        request.setLongitude(26.1025);
        request.setType(ListingType.DONATION);
        request.setCategory(ProductCategory.BAKERY);

        Set<ConstraintViolation<CreateListingRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void saleListingWithZeroMinimumPrice_shouldPass() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Test Sale");
        request.setQuantity(5);
        request.setPrice(new BigDecimal("10.00"));
        request.setMinimumPrice(BigDecimal.ZERO);
        request.setExpirationDate(LocalDateTime.now().plusDays(1));
        request.setLatitude(44.4268);
        request.setLongitude(26.1025);
        request.setType(ListingType.SALE);
        request.setCategory(ProductCategory.PIZZA);

        Set<ConstraintViolation<CreateListingRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
