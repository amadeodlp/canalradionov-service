package com.amadeodlp.canalradionov.app.web.controller.interaction;

import com.amadeodlp.canalradionov.core.model.interaction.Comment;
import com.amadeodlp.canalradionov.core.model.interaction.CommentRequest;
import com.amadeodlp.canalradionov.core.services.interaction.InteractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interaction")
public class InteractionController {
    private static final Logger LOG = LoggerFactory.getLogger(InteractionController.class);
    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/comments")
    public ResponseEntity<Comment> addComment(@RequestBody CommentRequest request) {
        LOG.info("Adding comment for {} {}", request.targetType(), request.targetId());
        return ResponseEntity.ok(interactionService.addComment(request));
    }

    @GetMapping("/comments")
    public ResponseEntity<List<Comment>> getComments(
            @RequestParam String targetType,
            @RequestParam String targetId) {
        LOG.info("Getting comments for {} {}", targetType, targetId);
        return ResponseEntity.ok(interactionService.getComments(targetType, targetId));
    }

    @PostMapping("/likes/show/{showId}")
    public ResponseEntity<Void> likeShow(@PathVariable String showId) {
        LOG.info("Liking show {}", showId);
        interactionService.likeShow(showId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/likes/show/{showId}")
    public ResponseEntity<Void> unlikeShow(@PathVariable String showId) {
        LOG.info("Unliking show {}", showId);
        interactionService.unlikeShow(showId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/likes/episode/{episodeId}")
    public ResponseEntity<Void> likeEpisode(@PathVariable String episodeId) {
        LOG.info("Liking episode {}", episodeId);
        interactionService.likeEpisode(episodeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/likes/episode/{episodeId}")
    public ResponseEntity<Void> unlikeEpisode(@PathVariable String episodeId) {
        LOG.info("Unliking episode {}", episodeId);
        interactionService.unlikeEpisode(episodeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/likes/check")
    public ResponseEntity<Map<String, Boolean>> checkLikeStatus(
            @RequestParam String targetType,
            @RequestParam String targetId) {
        LOG.info("Checking like status for {} {}", targetType, targetId);
        boolean liked = interactionService.hasLiked(targetType, targetId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @GetMapping("/likes/count")
    public ResponseEntity<Map<String, Integer>> getLikeCount(
            @RequestParam String targetType,
            @RequestParam String targetId) {
        LOG.info("Getting like count for {} {}", targetType, targetId);
        int count = interactionService.getLikeCount(targetType, targetId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/share")
    public ResponseEntity<Map<String, String>> shareContent(
            @RequestBody Map<String, String> request) {
        String targetType = request.get("targetType");
        String targetId = request.get("targetId");
        LOG.info("Generating share URL for {} {}", targetType, targetId);
        String shareUrl = interactionService.generateShareUrl(targetType, targetId);
        return ResponseEntity.ok(Map.of("shareUrl", shareUrl));
    }
}
