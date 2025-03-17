package com.amadeodlp.canalradionov.core.model.broadcast;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents an active broadcast session
 */
public record BroadcastSession(
    String id,
    String hostId,
    String hostName,
    String title,
    String description,
    List<String> tags,
    List<CoHost> coHosts,
    LocalDateTime startTime,
    String streamUrl,
    String status,
    String recordingUrl,
    int listenerCount,
    boolean isPrivate
) {
    /**
     * Represents a co-host in a broadcast
     */
    public record CoHost(
        String userId,
        String userName,
        LocalDateTime joinedAt,
        boolean isActive
    ) {}
}
