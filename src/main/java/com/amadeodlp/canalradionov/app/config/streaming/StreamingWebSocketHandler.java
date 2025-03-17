package com.amadeodlp.canalradionov.app.config.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StreamingWebSocketHandler extends TextWebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(StreamingWebSocketHandler.class);
    
    private final S3Client s3Client;
    private final String cloudfrontDomain;
    private final String bucketName;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    public StreamingWebSocketHandler(S3Client s3Client, String cloudfrontDomain, String bucketName) {
        this.s3Client = s3Client;
        this.cloudfrontDomain = cloudfrontDomain;
        this.bucketName = bucketName;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOG.info("WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        LOG.info("Received message from session {}: {}", session.getId(), payload);
        
        // Example: Parse the payload as JSON to determine what action to take
        // This is a simplified example; you would typically use a proper JSON library
        if (payload.contains("\"action\":\"play\"")) {
            String mediaId = extractMediaId(payload);
            sendMediaStreamInfo(session, mediaId);
        } else if (payload.contains("\"action\":\"pauseStream\"")) {
            // Handle pause action
            session.sendMessage(new TextMessage("{\"status\":\"paused\"}"));
        } else if (payload.contains("\"action\":\"resumeStream\"")) {
            // Handle resume action
            session.sendMessage(new TextMessage("{\"status\":\"resumed\"}"));
        } else if (payload.contains("\"action\":\"stopStream\"")) {
            // Handle stop action
            session.sendMessage(new TextMessage("{\"status\":\"stopped\"}"));
        } else {
            session.sendMessage(new TextMessage("{\"error\":\"Unknown action\"}"));
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOG.info("WebSocket connection closed: {} with status {}", session.getId(), status);
        sessions.remove(session.getId());
    }
    
    private String extractMediaId(String payload) {
        // Simplified extraction, you would use proper JSON parsing in production
        int startIdx = payload.indexOf("\"mediaId\":\"") + 11;
        int endIdx = payload.indexOf("\"", startIdx);
        return payload.substring(startIdx, endIdx);
    }
    
    private void sendMediaStreamInfo(WebSocketSession session, String mediaId) throws IOException {
        try {
            // In a real implementation, this would fetch the media URL from your database
            // and potentially sign the URL for secure access
            
            String mediaObjectKey = "media/" + mediaId + ".mp3";
            
            // Check if the media exists in S3
            try {
                s3Client.getObject(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(mediaObjectKey)
                        .build());
            } catch (Exception e) {
                LOG.error("Media not found: {}", mediaId, e);
                session.sendMessage(new TextMessage("{\"error\":\"Media not found\"}"));
                return;
            }
            
            // Generate a streaming URL
            String streamingUrl;
            if (cloudfrontDomain != null && !cloudfrontDomain.isEmpty()) {
                // Use CloudFront if configured
                streamingUrl = "https://" + cloudfrontDomain + "/" + mediaObjectKey;
            } else {
                // Fallback to direct S3 URL
                streamingUrl = "https://" + bucketName + ".s3." + s3Client.serviceClientConfiguration().region() + ".amazonaws.com/" + mediaObjectKey;
            }
            
            // Create a session record to track streaming statistics
            String sessionId = UUID.randomUUID().toString();
            String sessionData = String.format(
                    "{\"sessionId\":\"%s\",\"mediaId\":\"%s\",\"userId\":\"%s\",\"startTime\":\"%s\"}",
                    sessionId,
                    mediaId,
                    "user123", // This would come from your authentication system
                    Instant.now().toString()
            );
            
            // Store session data in S3 for analytics
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key("analytics/sessions/" + sessionId + ".json")
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromByteBuffer(ByteBuffer.wrap(sessionData.getBytes(StandardCharsets.UTF_8)))
            );
            
            // Send the streaming URL to the client
            String response = String.format(
                    "{\"status\":\"ready\",\"streamUrl\":\"%s\",\"sessionId\":\"%s\"}",
                    streamingUrl,
                    sessionId
            );
            
            session.sendMessage(new TextMessage(response));
        } catch (Exception e) {
            LOG.error("Error sending media stream info", e);
            session.sendMessage(new TextMessage("{\"error\":\"Internal server error\"}"));
        }
    }
}
