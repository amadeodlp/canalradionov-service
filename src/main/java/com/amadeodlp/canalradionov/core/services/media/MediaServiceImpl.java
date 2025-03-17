package com.amadeodlp.canalradionov.core.services.media;

import com.amadeodlp.canalradionov.core.model.media.Episode;
import com.amadeodlp.canalradionov.core.model.media.RadioShow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MediaServiceImpl implements MediaService {
    private static final Logger LOG = LoggerFactory.getLogger(MediaServiceImpl.class);
    
    // Mock data for initial development - would be replaced with actual data storage
    private final Map<String, RadioShow> shows = new HashMap<>();
    private final Map<String, Map<String, Episode>> episodes = new HashMap<>();
    
    public MediaServiceImpl() {
        // Initialize with some sample data
        initializeSampleData();
    }
    
    @Override
    public List<RadioShow> getAllShows() {
        LOG.info("Fetching all shows");
        return new ArrayList<>(shows.values());
    }
    
    @Override
    public RadioShow getShowById(String id) {
        LOG.info("Fetching show with id: {}", id);
        return shows.get(id);
    }
    
    @Override
    public List<RadioShow> getLiveShows() {
        LOG.info("Fetching live shows");
        LocalDateTime now = LocalDateTime.now();
        return shows.values().stream()
                .filter(show -> show.isLive() && 
                        show.scheduledTime().isBefore(now) && 
                        show.endTime().isAfter(now))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RadioShow> getUpcomingShows() {
        LOG.info("Fetching upcoming shows");
        LocalDateTime now = LocalDateTime.now();
        return shows.values().stream()
                .filter(show -> show.scheduledTime().isAfter(now))
                .collect(Collectors.toList());
    }
    
    @Override
    public Episode getEpisode(String showId, String episodeId) {
        LOG.info("Fetching episode {} for show {}", episodeId, showId);
        Map<String, Episode> showEpisodes = episodes.get(showId);
        if (showEpisodes != null) {
            return showEpisodes.get(episodeId);
        }
        return null;
    }
    
    @Override
    public void incrementPlayCount(String showId, String episodeId) {
        LOG.info("Incrementing play count for episode {} of show {}", episodeId, showId);
        Map<String, Episode> showEpisodes = episodes.get(showId);
        if (showEpisodes != null) {
            Episode episode = showEpisodes.get(episodeId);
            if (episode != null) {
                Episode updatedEpisode = new Episode(
                        episode.id(),
                        episode.title(),
                        episode.description(),
                        episode.audioUrl(),
                        episode.durationSeconds(),
                        episode.publishDate(),
                        episode.playCount() + 1,
                        episode.imageUrl()
                );
                showEpisodes.put(episodeId, updatedEpisode);
            }
        }
    }
    
    @Override
    public List<RadioShow> searchShows(String query) {
        LOG.info("Searching shows with query: {}", query);
        String lowercaseQuery = query.toLowerCase();
        return shows.values().stream()
                .filter(show -> 
                        show.title().toLowerCase().contains(lowercaseQuery) || 
                        show.description().toLowerCase().contains(lowercaseQuery) ||
                        show.hostName().toLowerCase().contains(lowercaseQuery) ||
                        show.tags().stream().anyMatch(tag -> tag.toLowerCase().contains(lowercaseQuery)))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RadioShow> getFeaturedShows() {
        LOG.info("Fetching featured shows");
        // In a real implementation, this would fetch shows based on some criteria like popularity
        // For now, we'll just return a limited set of shows
        return shows.values().stream()
                .limit(5)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<RadioShow> getRecommendedShows() {
        LOG.info("Fetching recommended shows");
        // In a real implementation, this would use user preferences and history
        // For now, we'll just return a random subset of shows
        List<RadioShow> allShows = new ArrayList<>(shows.values());
        Collections.shuffle(allShows);
        return allShows.stream()
                .limit(5)
                .collect(Collectors.toList());
    }
    
    // Sample data initialization method
    private void initializeSampleData() {
        // Create some episodes
        Map<String, Episode> show1Episodes = new HashMap<>();
        Episode episode1 = new Episode(
                "ep1",
                "Episode 1: Introduction to Jazz",
                "An exploration of jazz fundamentals and history",
                "/audio/jazz_intro.mp3",
                3600,
                LocalDateTime.now().minusDays(30),
                150,
                "/images/jazz_intro.jpg"
        );
        Episode episode2 = new Episode(
                "ep2",
                "Episode 2: The Bebop Revolution",
                "How bebop changed jazz forever",
                "/audio/bebop_revolution.mp3",
                3800,
                LocalDateTime.now().minusDays(15),
                120,
                "/images/bebop.jpg"
        );
        show1Episodes.put("ep1", episode1);
        show1Episodes.put("ep2", episode2);
        episodes.put("show1", show1Episodes);
        
        // Create another set of episodes
        Map<String, Episode> show2Episodes = new HashMap<>();
        Episode episode3 = new Episode(
                "ep1",
                "Classic Rock Origins",
                "The birth of rock and roll",
                "/audio/rock_origins.mp3",
                3200,
                LocalDateTime.now().minusDays(20),
                200,
                "/images/rock_origins.jpg"
        );
        show2Episodes.put("ep1", episode3);
        episodes.put("show2", show2Episodes);
        
        // Create some shows
        RadioShow show1 = new RadioShow(
                "show1",
                "Jazz Explorations",
                "A journey through the world of jazz music",
                "Maria Johnson",
                "/images/jazz_explorations.jpg",
                true,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                List.of("jazz", "music history", "bebop"),
                List.of(episode1, episode2)
        );
        
        RadioShow show2 = new RadioShow(
                "show2",
                "Rock Classics",
                "Revisiting the greatest rock hits",
                "David Thompson",
                "/images/rock_classics.jpg",
                false,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                List.of("rock", "classic rock", "60s", "70s"),
                List.of(episode3)
        );
        
        RadioShow show3 = new RadioShow(
                "show3",
                "Electronic Beats",
                "The latest in electronic music",
                "Sarah Williams",
                "/images/electronic_beats.jpg",
                false,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                List.of("electronic", "techno", "house"),
                List.of()
        );
        
        // Add shows to the map
        shows.put("show1", show1);
        shows.put("show2", show2);
        shows.put("show3", show3);
    }
}
