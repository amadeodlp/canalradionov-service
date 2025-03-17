package com.amadeodlp.canalradionov.core.model.broadcast;

import java.util.List;

/**
 * Request model for starting a broadcast
 */
public record BroadcastRequest(
    String title,
    String description,
    List<String> tags,
    List<String> coHostIds,
    boolean isPrivate
) {}
