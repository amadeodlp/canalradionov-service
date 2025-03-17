package com.amadeodlp.canalradionov.app.web.controller.media;

import com.amadeodlp.canalradionov.core.model.media.Episode;
import com.amadeodlp.canalradionov.core.model.media.RadioShow;
import com.amadeodlp.canalradionov.core.services.media.MediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    private static final Logger LOG = LoggerFactory.getLogger(MediaController.class);
    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/shows")
    public ResponseEntity<List<RadioShow>> getAllShows() {
        LOG.info("Getting all shows");
        return ResponseEntity.ok(mediaService.getAllShows());
    }

    @GetMapping("/shows/{id}")
    public ResponseEntity<RadioShow> getShowById(@PathVariable String id) {
        LOG.info("Getting show by id: {}", id);
        return ResponseEntity.ok(mediaService.getShowById(id));
    }

    @GetMapping("/shows/live")
    public ResponseEntity<List<RadioShow>> getLiveShows() {
        LOG.info("Getting live shows");
        return ResponseEntity.ok(mediaService.getLiveShows());
    }

    @GetMapping("/shows/upcoming")
    public ResponseEntity<List<RadioShow>> getUpcomingShows() {
        LOG.info("Getting upcoming shows");
        return ResponseEntity.ok(mediaService.getUpcomingShows());
    }

    @GetMapping("/shows/{showId}/episodes/{episodeId}")
    public ResponseEntity<Episode> getEpisode(@PathVariable String showId, @PathVariable String episodeId) {
        LOG.info("Getting episode {} for show {}", episodeId, showId);
        return ResponseEntity.ok(mediaService.getEpisode(showId, episodeId));
    }

    @PostMapping("/shows/{showId}/episodes/{episodeId}/play")
    public ResponseEntity<Void> incrementPlayCount(@PathVariable String showId, @PathVariable String episodeId) {
        LOG.info("Incrementing play count for episode {} of show {}", episodeId, showId);
        mediaService.incrementPlayCount(showId, episodeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<RadioShow>> searchShows(@RequestParam String q) {
        LOG.info("Searching shows with query: {}", q);
        return ResponseEntity.ok(mediaService.searchShows(q));
    }

    @GetMapping("/shows/featured")
    public ResponseEntity<List<RadioShow>> getFeaturedShows() {
        LOG.info("Getting featured shows");
        return ResponseEntity.ok(mediaService.getFeaturedShows());
    }

    @GetMapping("/shows/recommended")
    public ResponseEntity<List<RadioShow>> getRecommendedShows() {
        LOG.info("Getting recommended shows");
        return ResponseEntity.ok(mediaService.getRecommendedShows());
    }
}
