package com.amadeodlp.canalradionov.core.model;

import com.amadeodlp.canalradionov.core.auth.Role;
import java.util.UUID;

public record SessionResponse(
    UUID sessionId,
    String userId,
    String username,
    Role role
) {}
