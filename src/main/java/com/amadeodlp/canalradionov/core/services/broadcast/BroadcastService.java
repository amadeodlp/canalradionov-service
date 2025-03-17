package com.amadeodlp.canalradionov.core.services.broadcast;

import com.amadeodlp.canalradionov.core.model.broadcast.ActiveBroadcast;
import com.amadeodlp.canalradionov.core.model.broadcast.BroadcastRequest;
import com.amadeodlp.canalradionov.core.model.broadcast.BroadcastSession;

import java.util.List;

/**
 * Service for managing live broadcast sessions
 */
public interface BroadcastService {
    
    /**
     * Start a new broadcast session
     * 
     * @param userId The user ID of the host
     * @param request The broadcast details
     * @return The created broadcast session
     */
    BroadcastSession startBroadcast(String userId, BroadcastRequest request);
    
    /**
     * Stop an active broadcast session
     * 
     * @param sessionId The broadcast session ID
     * @param userId The user ID requesting the stop (must be host)
     * @return The finalized broadcast session
     */
    BroadcastSession stopBroadcast(String sessionId, String userId);
    
    /**
     * Get all currently active broadcasts
     * 
     * @return List of active broadcasts
     */
    List<ActiveBroadcast> getActiveBroadcasts();
    
    /**
     * Get a specific broadcast by ID
     * 
     * @param sessionId The broadcast session ID
     * @return The broadcast details or null if not found
     */
    ActiveBroadcast getBroadcastById(String sessionId);
    
    /**
     * Add a co-host to a broadcast
     * 
     * @param sessionId The broadcast session ID
     * @param hostId The host user ID (must be the original host)
     * @param coHostId The user ID to add as co-host
     * @return Updated broadcast session
     */
    BroadcastSession addCoHost(String sessionId, String hostId, String coHostId);
    
    /**
     * Remove a co-host from a broadcast
     * 
     * @param sessionId The broadcast session ID
     * @param hostId The host user ID (must be the original host)
     * @param coHostId The user ID to remove
     * @return Updated broadcast session
     */
    BroadcastSession removeCoHost(String sessionId, String hostId, String coHostId);
    
    /**
     * Get listener count for a broadcast
     * 
     * @param sessionId The broadcast session ID
     * @return Current listener count
     */
    int getListenerCount(String sessionId);
    
    /**
     * Update broadcast session metadata
     * 
     * @param sessionId The broadcast session ID
     * @param userId The user ID requesting the update (must be host)
     * @param request Updated broadcast details
     * @return Updated broadcast session
     */
    BroadcastSession updateBroadcast(String sessionId, String userId, BroadcastRequest request);
}
