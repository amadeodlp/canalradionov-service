package com.amadeodlp.canalradionov.core.services.broadcast;

import com.amadeodlp.canalradionov.core.model.broadcast.ActiveBroadcast;
import com.amadeodlp.canalradionov.core.model.broadcast.BroadcastRequest;
import com.amadeodlp.canalradionov.core.model.broadcast.BroadcastSession;
import com.amadeodlp.canalradionov.core.services.user.UserService;
import com.amadeodlp.canalradionov.core.model.User;
import com.amadeodlp.canalradionov.core.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BroadcastServiceImpl implements BroadcastService {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastServiceImpl.class);
    
    // In-memory storage for active broadcast sessions
    // In a production environment, this would be stored in a database
    private final Map<String, BroadcastSession> activeBroadcasts = new ConcurrentHashMap<>();
    
    // Track listener counts for each broadcast
    private final Map<String, Integer> listenerCounts = new ConcurrentHashMap<>();
    
    // Track listener sessions
    private final Map<String, Map<String, LocalDateTime>> listenerSessions = new ConcurrentHashMap<>();
    
    private final UserService userService;
    
    public BroadcastServiceImpl(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public BroadcastSession startBroadcast(String userId, BroadcastRequest request) {
        LOG.info("Starting broadcast for user: {}", userId);
        
        // Get user data
        User user = userService.getUserById(userId);
        if (user == null) {
            LOG.error("User not found: {}", userId);
            throw new IllegalArgumentException("User not found");
        }
        
        // Generate unique session ID
        String sessionId = UUID.randomUUID().toString();
        
        // Initialize co-hosts list if applicable
        List<BroadcastSession.CoHost> coHosts = new ArrayList<>();
        if (request.coHostIds() != null && !request.coHostIds().isEmpty()) {
            for (String coHostId : request.coHostIds()) {
                User coHostUser = userService.getUserById(coHostId);
                if (coHostUser != null) {
                    coHosts.add(new BroadcastSession.CoHost(
                        coHostId,
                        coHostUser.getName(),
                        LocalDateTime.now(),
                        false // Not active until they join
                    ));
                }
            }
        }
        
        // Create stream URL (in a real implementation, this would generate a secure streaming endpoint)
        String streamUrl = "wss://stream.example.com/broadcast/" + sessionId;
        
        // Create new broadcast session
        BroadcastSession session = new BroadcastSession(
            sessionId,
            userId,
            user.getName(),
            request.title(),
            request.description(),
            request.tags() != null ? request.tags() : new ArrayList<>(),
            coHosts,
            LocalDateTime.now(),
            streamUrl,
            "live",
            null, // Recording not available yet
            0, // Initial listener count
            request.isPrivate()
        );
        
        // Store in active broadcasts
        activeBroadcasts.put(sessionId, session);
        
        // Initialize listener tracking
        listenerCounts.put(sessionId, 0);
        listenerSessions.put(sessionId, new ConcurrentHashMap<>());
        
        LOG.info("Broadcast started successfully: {}", sessionId);
        return session;
    }
    
    @Override
    public BroadcastSession stopBroadcast(String sessionId, String userId) {
        LOG.info("Stopping broadcast: {}", sessionId);
        
        // Get broadcast session
        BroadcastSession session = activeBroadcasts.get(sessionId);
        if (session == null) {
            LOG.warn("Broadcast session not found: {}", sessionId);
            throw new IllegalArgumentException("Broadcast session not found");
        }
        
        // Verify user is the host
        if (!session.hostId().equals(userId)) {
            LOG.warn("User {} is not authorized to stop broadcast {}", userId, sessionId);
            throw new UnauthorizedException("Only the host can stop the broadcast");
        }
        
        // Create final session with recording URL
        // In a real implementation, this would process and store the recording
        String recordingUrl = "https://storage.example.com/recordings/" + sessionId + ".mp3";
        
        BroadcastSession finalSession = new BroadcastSession(
            session.id(),
            session.hostId(),
            session.hostName(),
            session.title(),
            session.description(),
            session.tags(),
            session.coHosts(),
            session.startTime(),
            session.streamUrl(),
            "ended",
            recordingUrl,
            listenerCounts.getOrDefault(sessionId, 0),
            session.isPrivate()
        );
        
        // Remove from active broadcasts
        activeBroadcasts.remove(sessionId);
        
        // Clear listener tracking
        listenerCounts.remove(sessionId);
        listenerSessions.remove(sessionId);
        
        LOG.info("Broadcast ended successfully: {}", sessionId);
        
        // In a real implementation, this would be stored in a database
        // for historical records
        
        return finalSession;
    }
    
    @Override
    public List<ActiveBroadcast> getActiveBroadcasts() {
        LOG.info("Getting all active broadcasts");
        
        // Convert internal sessions to public ActiveBroadcast objects
        return activeBroadcasts.values().stream()
            .filter(session -> !session.isPrivate()) // Filter out private sessions
            .map(this::mapToActiveBroadcast)
            .collect(Collectors.toList());
    }
    
    @Override
    public ActiveBroadcast getBroadcastById(String sessionId) {
        LOG.info("Getting broadcast details for: {}", sessionId);
        
        BroadcastSession session = activeBroadcasts.get(sessionId);
        if (session == null) {
            LOG.warn("Broadcast session not found: {}", sessionId);
            return null;
        }
        
        return mapToActiveBroadcast(session);
    }
    
    @Override
    public BroadcastSession addCoHost(String sessionId, String hostId, String coHostId) {
        LOG.info("Adding co-host {} to broadcast {}", coHostId, sessionId);
        
        // Get broadcast session
        BroadcastSession session = activeBroadcasts.get(sessionId);
        if (session == null) {
            LOG.warn("Broadcast session not found: {}", sessionId);
            throw new IllegalArgumentException("Broadcast session not found");
        }
        
        // Verify user is the host
        if (!session.hostId().equals(hostId)) {
            LOG.warn("User {} is not authorized to add co-hosts to broadcast {}", hostId, sessionId);
            throw new UnauthorizedException("Only the host can add co-hosts");
        }
        
        // Check if user already exists as co-host
        boolean coHostExists = session.coHosts().stream()
            .anyMatch(coHost -> coHost.userId().equals(coHostId));
        
        if (coHostExists) {
            LOG.info("User {} is already a co-host for broadcast {}", coHostId, sessionId);
            return session;
        }
        
        // Get co-host user data
        User coHostUser = userService.getUserById(coHostId);
        if (coHostUser == null) {
            LOG.error("Co-host user not found: {}", coHostId);
            throw new IllegalArgumentException("Co-host user not found");
        }
        
        // Create new co-host
        BroadcastSession.CoHost newCoHost = new BroadcastSession.CoHost(
            coHostId,
            coHostUser.getName(),
            LocalDateTime.now(),
            false // Not active until they join
        );
        
        // Add to co-hosts list
        List<BroadcastSession.CoHost> updatedCoHosts = new ArrayList<>(session.coHosts());
        updatedCoHosts.add(newCoHost);
        
        // Create updated session
        BroadcastSession updatedSession = new BroadcastSession(
            session.id(),
            session.hostId(),
            session.hostName(),
            session.title(),
            session.description(),
            session.tags(),
            updatedCoHosts,
            session.startTime(),
            session.streamUrl(),
            session.status(),
            session.recordingUrl(),
            listenerCounts.getOrDefault(sessionId, 0),
            session.isPrivate()
        );
        
        // Update stored session
        activeBroadcasts.put(sessionId, updatedSession);
        
        LOG.info("Co-host added successfully to broadcast {}", sessionId);
        return updatedSession;
    }
    
    @Override
    public BroadcastSession removeCoHost(String sessionId, String hostId, String coHostId) {
        LOG.info("Removing co-host {} from broadcast {}", coHostId, sessionId);
        
        // Get broadcast session
        BroadcastSession session = activeBroadcasts.get(sessionId);
        if (session == null) {
            LOG.warn("Broadcast session not found: {}", sessionId);
            throw new IllegalArgumentException("Broadcast session not found");
        }
        
        // Verify user is the host
        if (!session.hostId().equals(hostId)) {
            LOG.warn("User {} is not authorized to remove co-hosts from broadcast {}", hostId, sessionId);
            throw new UnauthorizedException("Only the host can remove co-hosts");
        }
        
        // Remove co-host
        List<BroadcastSession.CoHost> updatedCoHosts = session.coHosts().stream()
            .filter(coHost -> !coHost.userId().equals(coHostId))
            .collect(Collectors.toList());
        
        // Create updated session
        BroadcastSession updatedSession = new BroadcastSession(
            session.id(),
            session.hostId(),
            session.hostName(),
            session.title(),
            session.description(),
            session.tags(),
            updatedCoHosts,
            session.startTime(),
            session.streamUrl(),
            session.status(),
            session.recordingUrl(),
            listenerCounts.getOrDefault(sessionId, 0),
            session.isPrivate()
        );
        
        // Update stored session
        activeBroadcasts.put(sessionId, updatedSession);
        
        LOG.info("Co-host removed successfully from broadcast {}", sessionId);
        return updatedSession;
    }
    
    @Override
    public int getListenerCount(String sessionId) {
        return listenerCounts.getOrDefault(sessionId, 0);
    }
    
    @Override
    public BroadcastSession updateBroadcast(String sessionId, String userId, BroadcastRequest request) {
        LOG.info("Updating broadcast session: {}", sessionId);
        
        // Get broadcast session
        BroadcastSession session = activeBroadcasts.get(sessionId);
        if (session == null) {
            LOG.warn("Broadcast session not found: {}", sessionId);
            throw new IllegalArgumentException("Broadcast session not found");
        }
        
        // Verify user is the host
        if (!session.hostId().equals(userId)) {
            LOG.warn("User {} is not authorized to update broadcast {}", userId, sessionId);
            throw new UnauthorizedException("Only the host can update the broadcast");
        }
        
        // Update co-hosts if needed
        List<BroadcastSession.CoHost> coHosts = session.coHosts();
        if (request.coHostIds() != null) {
            // Keep existing co-hosts that are still in the new list
            coHosts = session.coHosts().stream()
                .filter(coHost -> request.coHostIds().contains(coHost.userId()))
                .collect(Collectors.toList());
            
            // Add new co-hosts
            for (String coHostId : request.coHostIds()) {
                boolean exists = coHosts.stream()
                    .anyMatch(coHost -> coHost.userId().equals(coHostId));
                
                if (!exists) {
                    User coHostUser = userService.getUserById(coHostId);
                    if (coHostUser != null) {
                        coHosts.add(new BroadcastSession.CoHost(
                            coHostId,
                            coHostUser.getName(),
                            LocalDateTime.now(),
                            false
                        ));
                    }
                }
            }
        }
        
        // Create updated session
        BroadcastSession updatedSession = new BroadcastSession(
            session.id(),
            session.hostId(),
            session.hostName(),
            request.title() != null ? request.title() : session.title(),
            request.description() != null ? request.description() : session.description(),
            request.tags() != null ? request.tags() : session.tags(),
            coHosts,
            session.startTime(),
            session.streamUrl(),
            session.status(),
            session.recordingUrl(),
            listenerCounts.getOrDefault(sessionId, 0),
            request.isPrivate()
        );
        
        // Update stored session
        activeBroadcasts.put(sessionId, updatedSession);
        
        LOG.info("Broadcast updated successfully: {}", sessionId);
        return updatedSession;
    }
    
    /**
     * Track a listener joining a broadcast
     * @param sessionId Broadcast session ID
     * @param listenerId Listener user ID
     * @return Updated listener count
     */
    public int addListener(String sessionId, String listenerId) {
        LOG.info("Listener {} joined broadcast {}", listenerId, sessionId);
        
        if (!activeBroadcasts.containsKey(sessionId)) {
            LOG.warn("Cannot add listener to non-existent broadcast: {}", sessionId);
            return 0;
        }
        
        // Record listener session
        Map<String, LocalDateTime> listeners = listenerSessions.get(sessionId);
        listeners.put(listenerId, LocalDateTime.now());
        
        // Update count
        int count = listeners.size();
        listenerCounts.put(sessionId, count);
        
        return count;
    }
    
    /**
     * Track a listener leaving a broadcast
     * @param sessionId Broadcast session ID
     * @param listenerId Listener user ID
     * @return Updated listener count
     */
    public int removeListener(String sessionId, String listenerId) {
        LOG.info("Listener {} left broadcast {}", listenerId, sessionId);
        
        if (!activeBroadcasts.containsKey(sessionId)) {
            LOG.warn("Cannot remove listener from non-existent broadcast: {}", sessionId);
            return 0;
        }
        
        // Remove listener session
        Map<String, LocalDateTime> listeners = listenerSessions.get(sessionId);
        listeners.remove(listenerId);
        
        // Update count
        int count = listeners.size();
        listenerCounts.put(sessionId, count);
        
        return count;
    }
    
    /**
     * Helper method to convert internal BroadcastSession to public ActiveBroadcast
     */
    private ActiveBroadcast mapToActiveBroadcast(BroadcastSession session) {
        // Get host image URL (in a real implementation, this would come from the user service)
        String hostImageUrl = "https://example.com/avatars/" + session.hostId() + ".jpg";
        
        // Map co-hosts
        List<ActiveBroadcast.CoHost> coHosts = session.coHosts().stream()
            .filter(BroadcastSession.CoHost::isActive) // Only include active co-hosts
            .map(coHost -> {
                String coHostImageUrl = "https://example.com/avatars/" + coHost.userId() + ".jpg";
                return new ActiveBroadcast.CoHost(
                    coHost.userId(),
                    coHost.userName(),
                    coHostImageUrl
                );
            })
            .collect(Collectors.toList());
        
        // Calculate estimated end time (assume 2 hours by default)
        LocalDateTime estimatedEndTime = session.startTime().plus(2, ChronoUnit.HOURS);
        
        return new ActiveBroadcast(
            session.id(),
            session.hostId(),
            session.hostName(),
            hostImageUrl,
            session.title(),
            session.description(),
            session.tags(),
            coHosts,
            session.startTime(),
            estimatedEndTime,
            listenerCounts.getOrDefault(session.id(), 0),
            session.status()
        );
    }
}
