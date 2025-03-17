package com.amadeodlp.canalradionov.core.model;

import com.amadeodlp.canalradionov.core.auth.Role;
import java.time.LocalDateTime;

/**
 * Represents a user in the system
 */
public record User(
    String id,
    String username,
    String email,
    String passwordHash,
    Role role,
    LocalDateTime createdAt,
    LocalDateTime lastLogin
) {
}
