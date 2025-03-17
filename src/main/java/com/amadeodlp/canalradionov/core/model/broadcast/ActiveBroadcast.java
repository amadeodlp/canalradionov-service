package com.amadeodlp.canalradionov.core.model.broadcast;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Public representation of an active broadcast session
 */
public record ActiveBroadcast(
    String id,
    String hostId,
    String hostName,
    String hostImageUrl,
    String title,
    String description,
    List<String> tags,
    List<CoHost> coHosts,
    LocalDateTime startTime,
    LocalDateTime estimatedEndTime,
    int listenerCount,
    String status
) {
    /**
     * Public representation of a co-host
     */
    public record CoHost(
        String userId,
        String userName,
        String userImageUrl
    ) {}
}
