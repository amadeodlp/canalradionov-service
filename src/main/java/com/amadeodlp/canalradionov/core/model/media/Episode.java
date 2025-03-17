package com.amadeodlp.canalradionov.core.model.media;

import java.time.LocalDateTime;

public record Episode(
    String id,
    String title,
    String description,
    String audioUrl,
    int durationSeconds,
    LocalDateTime publishDate,
    int playCount,
    String imageUrl
) {}
