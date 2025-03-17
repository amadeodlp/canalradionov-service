package com.amadeodlp.canalradionov.core.model.interaction;

import java.time.LocalDateTime;

public record Like(
    String id,
    String userId,
    String targetId,
    String targetType,
    LocalDateTime timestamp
) {}
