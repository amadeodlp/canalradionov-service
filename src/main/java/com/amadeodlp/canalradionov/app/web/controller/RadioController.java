package com.amadeodlp.canalradionov.app.web.controller;

import com.amadeodlp.canalradionov.core.model.lastfm.TrackInfo;
import com.amadeodlp.canalradionov.core.model.radio.RadioFilter;
import com.amadeodlp.canalradionov.core.model.radio.RadioStationDto;
import com.amadeodlp.canalradionov.core.services.radio.LastFmService;
import com.amadeodlp.canalradionov.core.services.radio.RadioBrowserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/radio")
public class RadioController {
    private static final Logger LOG = LoggerFactory.getLogger(RadioController.class);
    
    private final RadioBrowserService radioBrowserService;
    private final LastFmService lastFmService;
    
    public RadioController(RadioBrowserService radioBrowserService, LastFmService lastFmService) {
        this.radioBrowserService = radioBrowserService;
        this.lastFmService = lastFmService;
    }
    
    @GetMapping("/stations")
    public ResponseEntity<List<RadioStationDto>> getStations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) List<String> genres,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean musicOnly,
            @RequestParam(required = false) Boolean onlyOnline,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) Boolean reverse) {
                
        RadioFilter filter = new RadioFilter(
            name, country, countryCode, genres, language, 
            musicOnly, onlyOnline, limit, offset, order, reverse
        );
        
        LOG.info("Getting radio stations with filter: {}", filter);
        List<RadioStationDto> stations = radioBrowserService.getStations(filter);
        
        return ResponseEntity.ok(stations);
    }
    
    @GetMapping("/stations/{stationId}")
    public ResponseEntity<RadioStationDto> getStation(@PathVariable String stationId) {
        LOG.info("Getting radio station with ID: {}", stationId);
        
        RadioStationDto station = radioBrowserService.getStation(stationId);
        
        if (station == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(station);
    }
    
    @GetMapping("/genres")
    public ResponseEntity<List<String>> getGenres() {
        LOG.info("Getting radio genres");
        
        List<String> genres = radioBrowserService.getGenres();
        
        return ResponseEntity.ok(genres);
    }
    
    @GetMapping("/countries")
    public ResponseEntity<List<RadioBrowserService.CountryInfo>> getCountries() {
        LOG.info("Getting radio countries");
        
        List<RadioBrowserService.CountryInfo> countries = radioBrowserService.getCountries();
        
        return ResponseEntity.ok(countries);
    }
    
    @GetMapping("/track-info")
    public ResponseEntity<TrackInfo> getTrackInfo(
            @RequestParam String artist,
            @RequestParam String track) {
        
        LOG.info("Getting track info for: {} - {}", artist, track);
        
        Optional<TrackInfo> trackInfo = lastFmService.getTrackInfo(artist, track);
        
        return trackInfo
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/track-search")
    public ResponseEntity<List<TrackInfo>> searchTracks(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        LOG.info("Searching for tracks matching: {}", query);
        
        List<TrackInfo> results = lastFmService.searchTracks(query, limit);
        
        return ResponseEntity.ok(results);
    }
}
