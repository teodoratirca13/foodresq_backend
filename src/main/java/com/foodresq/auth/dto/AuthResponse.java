package com.foodresq.auth.dto;

import com.foodresq.user.entity.Role;

public record AuthResponse(String token, Role role) {}
