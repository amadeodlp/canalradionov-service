package com.amadeodlp.canalradionov.core.services.interaction;

import com.amadeodlp.canalradionov.core.model.interaction.Comment;
import com.amadeodlp.canalradionov.core.model.interaction.CommentRequest;

import java.util.List;

public interface InteractionService {
    Comment addComment(CommentRequest request);
    
    List<Comment> getComments(String targetType, String targetId);
    
    void likeShow(String showId);
    
    void unlikeShow(String showId);
    
    void likeEpisode(String episodeId);
    
    void unlikeEpisode(String episodeId);
    
    boolean hasLiked(String targetType, String targetId);
    
    int getLikeCount(String targetType, String targetId);
    
    String generateShareUrl(String targetType, String targetId);
}
