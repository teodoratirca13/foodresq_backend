package com.foodresq.listing.controller;

import com.foodresq.auth.dto.AuthResponse;
import com.foodresq.auth.dto.RegisterRequest;
import com.foodresq.auth.service.AuthService;
import com.foodresq.listing.dto.CreateListingRequest;
import com.foodresq.listing.dto.ListingResponse;
import com.foodresq.listing.enums.ListingType;
import com.foodresq.listing.enums.ProductCategory;
import com.foodresq.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLWp3dC1zaWduaW5n",
        "app.jwt.expiration-ms=3600000"
})
class ListingControllerValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuthService authService;

    private String businessToken;

    @BeforeEach
    void setUp() {
        String email = "validation-test-" + System.currentTimeMillis() + "@test.com";
        AuthResponse response = authService.register(new RegisterRequest("Test Business", email, "password123", Role.BUSINESS));
        businessToken = response.token();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(businessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void createSaleListing_withoutMinimumPrice_shouldReturn400() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Pizza Sale");
        request.setDescription("Fresh pizza");
        request.setQuantity(5);
        request.setPrice(new BigDecimal("12.99"));
        request.setMinimumPrice(null);
        request.setExpirationDate(LocalDateTime.now().plusDays(1));
        request.setLatitude(44.4268);
        request.setLongitude(26.1025);
        request.setType(ListingType.SALE);
        request.setCategory(ProductCategory.PIZZA);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/listings",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("minimumPrice is required for SALE listings"));
    }

    @Test
    void createSaleListing_withMinimumPrice_shouldReturn200() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Pizza Sale");
        request.setDescription("Fresh pizza");
        request.setQuantity(5);
        request.setPrice(new BigDecimal("12.99"));
        request.setMinimumPrice(new BigDecimal("6.50"));
        request.setExpirationDate(LocalDateTime.now().plusDays(1));
        request.setLatitude(44.4268);
        request.setLongitude(26.1025);
        request.setType(ListingType.SALE);
        request.setCategory(ProductCategory.PIZZA);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/listings",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"type\":\"SALE\""));
    }

    @Test
    void createDonationListing_withoutMinimumPrice_shouldReturn200() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Bakery Donation");
        request.setDescription("Surplus bread");
        request.setQuantity(10);
        request.setPrice(BigDecimal.ZERO);
        request.setMinimumPrice(null);
        request.setExpirationDate(LocalDateTime.now().plusDays(1));
        request.setLatitude(44.4268);
        request.setLongitude(26.1025);
        request.setType(ListingType.DONATION);
        request.setCategory(ProductCategory.BAKERY);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/listings",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"type\":\"DONATION\""));
    }

    @Test
    void createDonationListing_withZeroMinimumPrice_shouldReturn200() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Bakery Donation");
        request.setDescription("Surplus bread");
        request.setQuantity(10);
        request.setPrice(BigDecimal.ZERO);
        request.setMinimumPrice(BigDecimal.ZERO);
        request.setExpirationDate(LocalDateTime.now().plusDays(1));
        request.setLatitude(44.4268);
        request.setLongitude(26.1025);
        request.setType(ListingType.DONATION);
        request.setCategory(ProductCategory.BAKERY);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/listings",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"type\":\"DONATION\""));
    }

    @Test
    void createSaleListing_withMinimumPriceHigherThanPrice_shouldReturn400() {
        CreateListingRequest request = new CreateListingRequest();
        request.setTitle("Pizza Sale");
        request.setDescription("Fresh pizza");
        request.setQuantity(5);
        request.setPrice(new BigDecimal("10.00"));
        request.setMinimumPrice(new BigDecimal("15.00"));
        request.setExpirationDate(LocalDateTime.now().plusDays(1));
        request.setLatitude(44.4268);
        request.setLongitude(26.1025);
        request.setType(ListingType.SALE);
        request.setCategory(ProductCategory.PIZZA);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/listings",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Minimum price cannot be higher than the initial price"));
    }
}
