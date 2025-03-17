package com.amadeodlp.canalradionov.core.model.interaction;

import java.time.LocalDateTime;

public record Comment(
    String id,
    String userId,
    String username,
    String userAvatar,
    String content,
    LocalDateTime timestamp,
    String replyTo
) {}
