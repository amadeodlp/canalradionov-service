package com.amadeodlp.canalradionov.app.config.chat;

import com.amadeodlp.canalradionov.core.model.broadcast.BroadcastSession;
import com.amadeodlp.canalradionov.core.services.broadcast.BroadcastService;
import com.amadeodlp.canalradionov.core.services.broadcast.BroadcastServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for chat functionality in broadcasts
 */
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    
    private final ObjectMapper objectMapper;
    private final BroadcastService broadcastService;
    
    // Store active sessions by session ID
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // Store broadcast chat rooms
    private final Map<String, List<WebSocketSession>> chatRooms = new ConcurrentHashMap<>();
    
    // Store user info for sessions
    private final Map<String, UserInfo> sessionUsers = new ConcurrentHashMap<>();
    
    // Store chat history
    private final Map<String, List<ChatMessage>> chatHistory = new ConcurrentHashMap<>();
    
    public ChatWebSocketHandler(ObjectMapper objectMapper, BroadcastService broadcastService) {
        this.objectMapper = objectMapper;
        this.broadcastService = broadcastService;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOG.info("Chat WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        LOG.debug("Received message: {}", payload);
        
        JsonNode jsonNode = objectMapper.readTree(payload);
        String action = jsonNode.get("action").asText();
        
        switch (action) {
            case "joinChat":
                handleJoinChat(session, jsonNode);
                break;
                
            case "sendMessage":
                handleSendMessage(session, jsonNode);
                break;
                
            case "leaveChat":
                handleLeaveChat(session, jsonNode);
                break;
                
            default:
                LOG.warn("Unknown action: {}", action);
                sendError(session, "Unknown action");
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOG.info("Chat WebSocket connection closed: {}", session.getId());
        
        // Remove session from all chat rooms
        UserInfo userInfo = sessionUsers.get(session.getId());
        if (userInfo != null && userInfo.broadcastId != null) {
            removeFromChatRoom(userInfo.broadcastId, session);
        }
        
        sessions.remove(session.getId());
        sessionUsers.remove(session.getId());
    }
    
    /**
     * Handle a user joining a chat room
     */
    private void handleJoinChat(WebSocketSession session, JsonNode jsonNode) throws IOException {
        String broadcastId = jsonNode.get("broadcastId").asText();
        String userId = jsonNode.get("userId").asText();
        String userName = jsonNode.get("userName").asText();
        boolean isHost = jsonNode.has("isHost") && jsonNode.get("isHost").asBoolean();
        
        LOG.info("User {} joining chat for broadcast {}", userId, broadcastId);
        
        // Store user info for this session
        sessionUsers.put(session.getId(), new UserInfo(userId, userName, broadcastId, isHost));
        
        // Add to chat room
        if (!chatRooms.containsKey(broadcastId)) {
            chatRooms.put(broadcastId, new ArrayList<>());
            chatHistory.put(broadcastId, new ArrayList<>());
        }
        
        List<WebSocketSession> room = chatRooms.get(broadcastId);
        room.add(session);
        
        // Send chat history to user
        sendChatHistory(session, broadcastId);
        
        // Notify all users about updated user count
        sendUserCountUpdate(broadcastId);
        
        // If this is a listener, update the listener count
        if (!isHost && broadcastService instanceof BroadcastServiceImpl) {
            ((BroadcastServiceImpl) broadcastService).addListener(broadcastId, userId);
        }
    }
    
    /**
     * Handle sending a chat message
     */
    private void handleSendMessage(WebSocketSession session, JsonNode jsonNode) throws IOException {
        String broadcastId = jsonNode.get("broadcastId").asText();
        String userId = jsonNode.get("userId").asText();
        String userName = jsonNode.get("userName").asText();
        String content = jsonNode.get("message").asText();
        
        LOG.info("User {} sending message to broadcast {}", userId, broadcastId);
        
        UserInfo userInfo = sessionUsers.get(session.getId());
        if (userInfo == null || !userInfo.userId.equals(userId)) {
            LOG.warn("User ID mismatch for session {}", session.getId());
            sendError(session, "User ID mismatch");
            return;
        }
        
        // Create message
        ChatMessage chatMessage = new ChatMessage(
            UUID.randomUUID().toString(),
            userId,
            userName,
            content,
            LocalDateTime.now().toString(),
            userInfo.isHost
        );
        
        // Store in history
        if (chatHistory.containsKey(broadcastId)) {
            List<ChatMessage> history = chatHistory.get(broadcastId);
            
            // Limit history size
            if (history.size() >= 100) {
                history.remove(0);
            }
            
            history.add(chatMessage);
        }
        
        // Broadcast to all users in the room
        broadcastMessage(broadcastId, chatMessage);
    }
    
    /**
     * Handle a user leaving a chat room
     */
    private void handleLeaveChat(WebSocketSession session, JsonNode jsonNode) throws IOException {
        String broadcastId = jsonNode.get("broadcastId").asText();
        
        UserInfo userInfo = sessionUsers.get(session.getId());
        if (userInfo == null) {
            LOG.warn("No user info for session {}", session.getId());
            return;
        }
        
        LOG.info("User {} leaving chat for broadcast {}", userInfo.userId, broadcastId);
        
        removeFromChatRoom(broadcastId, session);
        
        // If this is a listener, update the listener count
        if (!userInfo.isHost && broadcastService instanceof BroadcastServiceImpl) {
            ((BroadcastServiceImpl) broadcastService).removeListener(broadcastId, userInfo.userId);
        }
        
        // Clear user info
        sessionUsers.remove(session.getId());
        
        // Notify all users about updated user count
        sendUserCountUpdate(broadcastId);
    }
    
    /**
     * Send chat history to a user
     */
    private void sendChatHistory(WebSocketSession session, String broadcastId) throws IOException {
        if (!chatHistory.containsKey(broadcastId)) {
            return;
        }
        
        List<ChatMessage> history = chatHistory.get(broadcastId);
        if (history.isEmpty()) {
            return;
        }
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "history");
        
        ArrayNode messagesNode = response.putArray("messages");
        for (ChatMessage message : history) {
            ObjectNode messageNode = messagesNode.addObject();
            messageNode.put("id", message.id);
            messageNode.put("userId", message.userId);
            messageNode.put("userName", message.userName);
            messageNode.put("message", message.message);
            messageNode.put("timestamp", message.timestamp);
            messageNode.put("isHost", message.isHost);
        }
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
    
    /**
     * Broadcast a message to all users in a chat room
     */
    private void broadcastMessage(String broadcastId, ChatMessage message) throws IOException {
        if (!chatRooms.containsKey(broadcastId)) {
            return;
        }
        
        ObjectNode messageNode = objectMapper.createObjectNode();
        messageNode.put("type", "message");
        messageNode.put("id", message.id);
        messageNode.put("userId", message.userId);
        messageNode.put("userName", message.userName);
        messageNode.put("message", message.message);
        messageNode.put("timestamp", message.timestamp);
        messageNode.put("isHost", message.isHost);
        
        String messageJson = objectMapper.writeValueAsString(messageNode);
        TextMessage textMessage = new TextMessage(messageJson);
        
        List<WebSocketSession> room = chatRooms.get(broadcastId);
        List<WebSocketSession> inactiveSessions = new ArrayList<>();
        
        for (WebSocketSession userSession : room) {
            try {
                if (userSession.isOpen()) {
                    userSession.sendMessage(textMessage);
                } else {
                    inactiveSessions.add(userSession);
                }
            } catch (IOException e) {
                LOG.error("Error sending message to session {}", userSession.getId(), e);
                inactiveSessions.add(userSession);
            }
        }
        
        // Remove closed sessions
        for (WebSocketSession inactiveSession : inactiveSessions) {
            removeFromChatRoom(broadcastId, inactiveSession);
        }
    }
    
    /**
     * Send an update about the user count in a chat room
     */
    private void sendUserCountUpdate(String broadcastId) throws IOException {
        if (!chatRooms.containsKey(broadcastId)) {
            return;
        }
        
        List<WebSocketSession> room = chatRooms.get(broadcastId);
        int userCount = room.size();
        
        ObjectNode countNode = objectMapper.createObjectNode();
        countNode.put("type", "userCount");
        countNode.put("count", userCount);
        
        String countJson = objectMapper.writeValueAsString(countNode);
        TextMessage textMessage = new TextMessage(countJson);
        
        for (WebSocketSession userSession : room) {
            if (userSession.isOpen()) {
                try {
                    userSession.sendMessage(textMessage);
                } catch (IOException e) {
                    LOG.error("Error sending user count update to session {}", userSession.getId(), e);
                }
            }
        }
    }
    
    /**
     * Remove a session from a chat room
     */
    private void removeFromChatRoom(String broadcastId, WebSocketSession session) {
        if (!chatRooms.containsKey(broadcastId)) {
            return;
        }
        
        List<WebSocketSession> room = chatRooms.get(broadcastId);
        room.remove(session);
        
        // If room is empty, remove it
        if (room.isEmpty()) {
            chatRooms.remove(broadcastId);
            chatHistory.remove(broadcastId);
        }
    }
    
    /**
     * Send an error message to a session
     */
    private void sendError(WebSocketSession session, String errorMessage) throws IOException {
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("type", "error");
        errorNode.put("message", errorMessage);
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorNode)));
    }
    
    /**
     * Store user information for a session
     */
    private static class UserInfo {
        final String userId;
        final String userName;
        final String broadcastId;
        final boolean isHost;
        
        UserInfo(String userId, String userName, String broadcastId, boolean isHost) {
            this.userId = userId;
            this.userName = userName;
            this.broadcastId = broadcastId;
            this.isHost = isHost;
        }
    }
    
    /**
     * Represents a chat message
     */
    private static class ChatMessage {
        final String id;
        final String userId;
        final String userName;
        final String message;
        final String timestamp;
        final boolean isHost;
        
        ChatMessage(String id, String userId, String userName, String message, String timestamp, boolean isHost) {
            this.id = id;
            this.userId = userId;
            this.userName = userName;
            this.message = message;
            this.timestamp = timestamp;
            this.isHost = isHost;
        }
    }
}
