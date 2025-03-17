package com.amadeodlp.canalradionov.core.model.media;

import java.time.LocalDateTime;
import java.util.List;

public record RadioShow(
    String id,
    String title,
    String description,
    String hostName,
    String imageUrl,
    boolean isLive,
    LocalDateTime scheduledTime,
    LocalDateTime endTime,
    List<String> tags,
    List<Episode> episodes
) {}
