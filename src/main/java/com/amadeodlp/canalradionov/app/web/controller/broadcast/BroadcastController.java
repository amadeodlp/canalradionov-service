package com.amadeodlp.canalradionov.app.web.controller.broadcast;

import com.amadeodlp.canalradionov.core.model.broadcast.ActiveBroadcast;
import com.amadeodlp.canalradionov.core.model.broadcast.BroadcastRequest;
import com.amadeodlp.canalradionov.core.model.broadcast.BroadcastSession;
import com.amadeodlp.canalradionov.core.services.broadcast.BroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/broadcast")
public class BroadcastController {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastController.class);
    
    private final BroadcastService broadcastService;
    
    public BroadcastController(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }
    
    /**
     * Start a new broadcast session
     */
    @PostMapping("/start")
    public ResponseEntity<BroadcastSession> startBroadcast(
            @RequestBody BroadcastRequest request,
            Authentication authentication) {
        
        LOG.info("Starting new broadcast: {}", request.title());
        String userId = authentication.getName();
        
        BroadcastSession session = broadcastService.startBroadcast(userId, request);
        return ResponseEntity.ok(session);
    }
    
    /**
     * Stop an active broadcast
     */
    @PostMapping("/stop/{sessionId}")
    public ResponseEntity<Void> stopBroadcast(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        LOG.info("Stopping broadcast session: {}", sessionId);
        String userId = authentication.getName();
        
        broadcastService.stopBroadcast(sessionId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get all currently active broadcasts
     */
    @GetMapping("/active")
    public ResponseEntity<List<ActiveBroadcast>> getActiveBroadcasts() {
        LOG.info("Getting all active broadcasts");
        
        List<ActiveBroadcast> broadcasts = broadcastService.getActiveBroadcasts();
        return ResponseEntity.ok(broadcasts);
    }
    
    /**
     * Get details for a specific broadcast
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ActiveBroadcast> getBroadcastDetails(@PathVariable String sessionId) {
        LOG.info("Getting details for broadcast: {}", sessionId);
        
        ActiveBroadcast broadcast = broadcastService.getBroadcastById(sessionId);
        if (broadcast == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(broadcast);
    }
    
    /**
     * Add a co-host to a broadcast
     */
    @PostMapping("/{sessionId}/cohosts/{userId}")
    public ResponseEntity<Void> addCoHost(
            @PathVariable String sessionId,
            @PathVariable String userId,
            Authentication authentication) {
        
        LOG.info("Adding co-host {} to broadcast {}", userId, sessionId);
        String hostId = authentication.getName();
        
        broadcastService.addCoHost(sessionId, hostId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Remove a co-host from a broadcast
     */
    @DeleteMapping("/{sessionId}/cohosts/{userId}")
    public ResponseEntity<Void> removeCoHost(
            @PathVariable String sessionId,
            @PathVariable String userId,
            Authentication authentication) {
        
        LOG.info("Removing co-host {} from broadcast {}", userId, sessionId);
        String hostId = authentication.getName();
        
        broadcastService.removeCoHost(sessionId, hostId, userId);
        return ResponseEntity.ok().build();
    }
}
