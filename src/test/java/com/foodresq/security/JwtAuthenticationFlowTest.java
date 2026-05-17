package com.foodresq.security;

import com.foodresq.auth.dto.AuthResponse;
import com.foodresq.auth.dto.LoginRequest;
import com.foodresq.auth.dto.RegisterRequest;
import com.foodresq.auth.service.AuthService;
import com.foodresq.user.entity.Role;
import com.foodresq.user.entity.User;
import com.foodresq.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtAuthenticationFlowTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void businessUserHasCorrectAuthority() throws Exception {
        String email = "authoritytest@test.com";
        cleanupUser(email);

        authService.register(new RegisterRequest("Test Business", email, "password123", Role.BUSINESS));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        boolean hasBusinessRole = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));

        assertTrue(hasBusinessRole, "BUSINESS user should have ROLE_BUSINESS authority");
        assertEquals(email, userDetails.getUsername(), "UserDetails.getUsername() should return email");
    }

    @Test
    void userRoleHasCorrectAuthority() throws Exception {
        String email = "authorityuser@test.com";
        cleanupUser(email);

        authService.register(new RegisterRequest("Test User", email, "password123", Role.USER));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        boolean hasUserRole = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        assertTrue(hasUserRole, "USER should have ROLE_USER authority");
    }

    @Test
    void ongRoleHasCorrectAuthority() throws Exception {
        String email = "authorityong@test.com";
        cleanupUser(email);

        authService.register(new RegisterRequest("Test ONG", email, "password123", Role.ONG));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        boolean hasOngRole = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ONG"));

        assertTrue(hasOngRole, "ONG should have ROLE_ONG authority");
    }

    @Test
    void jwtTokenContainsRole() {
        String email = "jwttest@test.com";
        cleanupUser(email);

        AuthResponse response = authService.register(new RegisterRequest("Test", email, "password123", Role.BUSINESS));

        assertNotNull(response.token());
        assertEquals(Role.BUSINESS, response.role());
    }

    private void cleanupUser(String email) {
        userRepository.findByEmail(email).ifPresent(userRepository::delete);
    }
}