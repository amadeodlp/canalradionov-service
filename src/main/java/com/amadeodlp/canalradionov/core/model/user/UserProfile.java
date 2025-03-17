package com.amadeodlp.canalradionov.core.model.user;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfile(
    String id,
    String username,
    String email,
    String displayName,
    String avatarUrl,
    String bio,
    LocalDateTime createdAt,
    List<String> favoriteShowIds
) {}
