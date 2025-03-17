package com.amadeodlp.canalradionov.core.services.radio;

import com.amadeodlp.canalradionov.core.model.lastfm.TrackInfo;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class LastFmService {
    private static final Logger LOG = LoggerFactory.getLogger(LastFmService.class);
    
    private final RestTemplate restTemplate;
    private final String API_KEY;
    private final String BASE_URL = "https://ws.audioscrobbler.com/2.0/";
    
    public LastFmService(
            RestTemplate restTemplate,
            @Value("${lastfm.api.key:REPLACE_WITH_YOUR_API_KEY}") String apiKey) {
        this.restTemplate = restTemplate;
        this.API_KEY = apiKey;
    }
    
    @Cacheable(value = "lastFm", key = "'track-' + #artist + '-' + #trackName")
    public Optional<TrackInfo> getTrackInfo(String artist, String trackName) {
        LOG.info("Fetching track info for: {} - {}", artist, trackName);
        
        if (artist == null || artist.isEmpty() || trackName == null || trackName.isEmpty()) {
            LOG.warn("Artist or track name is empty");
            return Optional.empty();
        }
        
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("method", "track.getInfo")
                    .queryParam("api_key", API_KEY)
                    .queryParam("artist", artist)
                    .queryParam("track", trackName)
                    .queryParam("format", "json")
                    .build()
                    .toUriString();
            
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            
            if (response == null || response.has("error")) {
                if (response != null && response.has("error")) {
                    LOG.warn("LastFm API error: {}", response.get("message").asText());
                }
                return Optional.empty();
            }
            
            JsonNode trackNode = response.get("track");
            if (trackNode == null) {
                LOG.warn("Track not found: {} - {}", artist, trackName);
                return Optional.empty();
            }
            
            String name = trackNode.get("name").asText();
            String artistName = trackNode.get("artist").get("name").asText();
            
            // Album might not be available
            String album = "";
            if (trackNode.has("album") && !trackNode.get("album").isNull()) {
                album = trackNode.get("album").get("title").asText();
            }
            
            String trackUrl = trackNode.get("url").asText();
            
            // Get the largest image
            String imageUrl = "";
            if (trackNode.has("album") && !trackNode.get("album").isNull() && 
                trackNode.get("album").has("image")) {
                JsonNode images = trackNode.get("album").get("image");
                for (JsonNode image : images) {
                    if (image.get("size").asText().equals("large") || 
                        image.get("size").asText().equals("extralarge")) {
                        String imgUrl = image.get("#text").asText();
                        if (!imgUrl.isEmpty()) {
                            imageUrl = imgUrl;
                        }
                    }
                }
            }
            
            int listeners = 0;
            if (trackNode.has("listeners") && !trackNode.get("listeners").isNull()) {
                listeners = trackNode.get("listeners").asInt();
            }
            
            int playCount = 0;
            if (trackNode.has("playcount") && !trackNode.get("playcount").isNull()) {
                playCount = trackNode.get("playcount").asInt();
            }
            
            String wiki = "";
            if (trackNode.has("wiki") && !trackNode.get("wiki").isNull() && 
                trackNode.get("wiki").has("content")) {
                wiki = trackNode.get("wiki").get("content").asText();
            }
            
            boolean isLoved = false;
            if (trackNode.has("userloved") && !trackNode.get("userloved").isNull()) {
                isLoved = trackNode.get("userloved").asBoolean();
            }
            
            // Extract tags
            List<String> tagList = new ArrayList<>();
            if (trackNode.has("toptags") && !trackNode.get("toptags").isNull() && 
                trackNode.get("toptags").has("tag")) {
                JsonNode tags = trackNode.get("toptags").get("tag");
                for (JsonNode tag : tags) {
                    tagList.add(tag.get("name").asText());
                }
            }
            
            String[] tags = tagList.toArray(new String[0]);
            
            TrackInfo trackInfo = new TrackInfo(
                name, artistName, album, trackUrl, imageUrl, 
                listeners, playCount, wiki, isLoved, tags
            );
            
            LOG.info("Successfully fetched track info for: {} - {}", artist, trackName);
            return Optional.of(trackInfo);
            
        } catch (Exception e) {
            LOG.error("Error fetching track info: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Cacheable(value = "lastFm", key = "'search-' + #searchTerm")
    public List<TrackInfo> searchTracks(String searchTerm, int limit) {
        LOG.info("Searching LastFm for tracks matching: {}", searchTerm);
        
        if (searchTerm == null || searchTerm.isEmpty()) {
            LOG.warn("Search term is empty");
            return new ArrayList<>();
        }
        
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("method", "track.search")
                    .queryParam("api_key", API_KEY)
                    .queryParam("track", searchTerm)
                    .queryParam("limit", limit)
                    .queryParam("format", "json")
                    .build()
                    .toUriString();
            
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            
            if (response == null || !response.has("results") || 
                !response.get("results").has("trackmatches") ||
                !response.get("results").get("trackmatches").has("track")) {
                return new ArrayList<>();
            }
            
            JsonNode tracks = response.get("results").get("trackmatches").get("track");
            List<TrackInfo> results = new ArrayList<>();
            
            for (JsonNode track : tracks) {
                String name = track.get("name").asText();
                String artist = track.get("artist").asText();
                String trackUrl = track.get("url").asText();
                
                // Try to get an image
                String imageUrl = "";
                if (track.has("image")) {
                    for (JsonNode image : track.get("image")) {
                        if (image.get("size").asText().equals("large")) {
                            String imgUrl = image.get("#text").asText();
                            if (!imgUrl.isEmpty()) {
                                imageUrl = imgUrl;
                                break;
                            }
                        }
                    }
                }
                
                int listeners = track.has("listeners") ? track.get("listeners").asInt() : 0;
                
                // Basic search result doesn't have all the info, so we use defaults
                TrackInfo trackInfo = new TrackInfo(
                    name, artist, "", trackUrl, imageUrl, 
                    listeners, 0, "", false, new String[0]
                );
                
                results.add(trackInfo);
            }
            
            return results;
            
        } catch (Exception e) {
            LOG.error("Error searching for tracks: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
