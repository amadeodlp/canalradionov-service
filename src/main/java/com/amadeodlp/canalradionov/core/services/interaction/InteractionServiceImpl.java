package com.amadeodlp.canalradionov.core.services.interaction;

import com.amadeodlp.canalradionov.core.model.interaction.Comment;
import com.amadeodlp.canalradionov.core.model.interaction.CommentRequest;
import com.amadeodlp.canalradionov.core.model.interaction.Like;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InteractionServiceImpl implements InteractionService {
    private static final Logger LOG = LoggerFactory.getLogger(InteractionServiceImpl.class);
    
    // Mock data storage - would be replaced with real database in production
    private final Map<String, List<Comment>> comments = new HashMap<>();
    private final Map<String, List<Like>> likes = new HashMap<>();
    private final String baseShareUrl = "https://canalradionov.com/share";
    
    // Typically this would be injected from authentication context
    private String getCurrentUserId() {
        // In a real implementation, this would get the user ID from the security context
        return "user123";
    }
    
    private String getCurrentUsername() {
        // Similarly, this would be from the security context
        return "johndoe";
    }
    
    @Override
    public Comment addComment(CommentRequest request) {
        LOG.info("Adding comment for {} {}", request.targetType(), request.targetId());
        
        // Create a new comment
        String commentId = UUID.randomUUID().toString();
        Comment comment = new Comment(
                commentId,
                getCurrentUserId(),
                getCurrentUsername(),
                "/images/default-avatar.png",
                request.content(),
                LocalDateTime.now(),
                request.replyTo()
        );
        
        // Get the target key
        String targetKey = request.targetType() + ":" + request.targetId();
        
        // Add the comment to our storage
        if (!comments.containsKey(targetKey)) {
            comments.put(targetKey, new ArrayList<>());
        }
        comments.get(targetKey).add(comment);
        
        return comment;
    }
    
    @Override
    public List<Comment> getComments(String targetType, String targetId) {
        LOG.info("Getting comments for {} {}", targetType, targetId);
        
        // Get the target key
        String targetKey = targetType + ":" + targetId;
        
        // Return the comments, or an empty list if none found
        return comments.getOrDefault(targetKey, new ArrayList<>());
    }
    
    @Override
    public void likeShow(String showId) {
        like("show", showId);
    }
    
    @Override
    public void unlikeShow(String showId) {
        unlike("show", showId);
    }
    
    @Override
    public void likeEpisode(String episodeId) {
        like("episode", episodeId);
    }
    
    @Override
    public void unlikeEpisode(String episodeId) {
        unlike("episode", episodeId);
    }
    
    private void like(String targetType, String targetId) {
        LOG.info("Adding like for {} {}", targetType, targetId);
        
        String userId = getCurrentUserId();
        String targetKey = targetType + ":" + targetId;
        
        // Check if user already liked this target
        if (!hasLiked(targetType, targetId)) {
            // Create a new like
            Like like = new Like(
                    UUID.randomUUID().toString(),
                    userId,
                    targetId,
                    targetType,
                    LocalDateTime.now()
            );
            
            // Add the like to our storage
            if (!likes.containsKey(targetKey)) {
                likes.put(targetKey, new ArrayList<>());
            }
            likes.get(targetKey).add(like);
        }
    }
    
    private void unlike(String targetType, String targetId) {
        LOG.info("Removing like for {} {}", targetType, targetId);
        
        String userId = getCurrentUserId();
        String targetKey = targetType + ":" + targetId;
        
        // Find and remove the like
        if (likes.containsKey(targetKey)) {
            List<Like> targetLikes = likes.get(targetKey);
            targetLikes.removeIf(like -> like.userId().equals(userId));
        }
    }
    
    @Override
    public boolean hasLiked(String targetType, String targetId) {
        LOG.info("Checking if user has liked {} {}", targetType, targetId);
        
        String userId = getCurrentUserId();
        String targetKey = targetType + ":" + targetId;
        
        // Check if user has liked this target
        if (likes.containsKey(targetKey)) {
            return likes.get(targetKey).stream()
                    .anyMatch(like -> like.userId().equals(userId));
        }
        
        return false;
    }
    
    @Override
    public int getLikeCount(String targetType, String targetId) {
        LOG.info("Getting like count for {} {}", targetType, targetId);
        
        String targetKey = targetType + ":" + targetId;
        
        // Count likes for this target
        if (likes.containsKey(targetKey)) {
            return likes.get(targetKey).size();
        }
        
        return 0;
    }
    
    @Override
    public String generateShareUrl(String targetType, String targetId) {
        LOG.info("Generating share URL for {} {}", targetType, targetId);
        
        // In a real implementation, this might create a short URL or a unique sharing token
        return baseShareUrl + "/" + targetType + "/" + targetId;
    }
}
