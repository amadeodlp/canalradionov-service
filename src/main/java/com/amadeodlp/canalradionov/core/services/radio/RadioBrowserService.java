package com.amadeodlp.canalradionov.core.services.radio;

import com.amadeodlp.canalradionov.core.model.radio.RadioFilter;
import com.amadeodlp.canalradionov.core.model.radio.RadioStation;
import com.amadeodlp.canalradionov.core.model.radio.RadioStationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RadioBrowserService {
    private static final Logger LOG = LoggerFactory.getLogger(RadioBrowserService.class);
    
    private final RestTemplate restTemplate;
    private final String BASE_URL = "https://de1.api.radio-browser.info/json";
    private final String USER_AGENT = "CanalRadioNov/1.0";
    
    @Autowired
    public RadioBrowserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Cacheable(value = "radioBrowser", key = "'stations-' + #filter.toString()")
    public List<RadioStationDto> getStations(RadioFilter filter) {
        LOG.info("Fetching radio stations with filter: {}", filter);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            String url = BASE_URL + "/stations/search?" + filter.toQueryParameters();
            LOG.debug("Request URL: {}", url);
            
            ResponseEntity<RadioStation[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                RadioStation[].class
            );
            
            RadioStation[] stations = response.getBody();
            
            if (stations == null || stations.length == 0) {
                LOG.info("No stations found for filter: {}", filter);
                return Collections.emptyList();
            }
            
            List<RadioStationDto> stationDtos = Arrays.stream(stations)
                    .map(RadioStation::toDto)
                    .collect(Collectors.toList());
            
            LOG.info("Found {} stations matching filter", stationDtos.size());
            return stationDtos;
            
        } catch (Exception e) {
            LOG.error("Error fetching radio stations: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Cacheable(value = "radioBrowser", key = "'station-' + #stationId")
    public RadioStationDto getStation(String stationId) {
        LOG.info("Fetching radio station with ID: {}", stationId);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            String url = BASE_URL + "/stations/byuuid/" + stationId;
            
            ResponseEntity<RadioStation[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                RadioStation[].class
            );
            
            RadioStation[] stations = response.getBody();
            
            if (stations == null || stations.length == 0) {
                LOG.warn("No station found with ID: {}", stationId);
                return null;
            }
            
            LOG.info("Successfully fetched station: {}", stations[0].name());
            return stations[0].toDto();
            
        } catch (Exception e) {
            LOG.error("Error fetching radio station: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Cacheable("radioGenres")
    public List<String> getGenres() {
        LOG.info("Fetching available radio genres");
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            String url = BASE_URL + "/tags";
            
            ResponseEntity<List<TagCount>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<TagCount>>() {}
            );
            
            List<TagCount> tags = response.getBody();
            
            if (tags == null || tags.isEmpty()) {
                LOG.warn("No genres found");
                return Collections.emptyList();
            }
            
            // Filter to music-related genres with at least 5 stations
            List<String> musicGenres = tags.stream()
                    .filter(tag -> tag.count >= 5)
                    .filter(tag -> isMusicGenre(tag.name))
                    .map(tag -> tag.name)
                    .sorted()
                    .collect(Collectors.toList());
            
            LOG.info("Found {} music genres", musicGenres.size());
            return musicGenres;
            
        } catch (Exception e) {
            LOG.error("Error fetching radio genres: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Cacheable("radioCountries")
    public List<CountryInfo> getCountries() {
        LOG.info("Fetching available radio countries");
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            String url = BASE_URL + "/countries";
            
            ResponseEntity<List<CountryInfo>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<CountryInfo>>() {}
            );
            
            List<CountryInfo> countries = response.getBody();
            
            if (countries == null || countries.isEmpty()) {
                LOG.warn("No countries found");
                return Collections.emptyList();
            }
            
            // Filter to countries with at least 5 stations
            List<CountryInfo> filteredCountries = countries.stream()
                    .filter(country -> country.stationCount >= 5)
                    .sorted((c1, c2) -> c2.stationCount - c1.stationCount)
                    .collect(Collectors.toList());
            
            LOG.info("Found {} countries with radio stations", filteredCountries.size());
            return filteredCountries;
            
        } catch (Exception e) {
            LOG.error("Error fetching radio countries: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    // Helper method to determine if a tag is a music genre
    private boolean isMusicGenre(String tag) {
        // Basic heuristic - we can expand this list
        String[] musicKeywords = {"music", "rock", "pop", "jazz", "hip hop", "electronic", "classical", "folk", 
                                 "indie", "blues", "country", "reggae", "metal", "punk", "soul", "r&b", "disco",
                                 "hits", "dance", "techno", "house", "top", "charts", "80s", "90s", "00s"};
        
        String lowerTag = tag.toLowerCase();
        return Arrays.stream(musicKeywords).anyMatch(lowerTag::contains);
    }
    
    // Helper classes for API responses
    private static class TagCount {
        public String name;
        public int count;
    }
    
    public static class CountryInfo {
        public String name;
        public String iso_3166_1;
        public int stationCount;
    }
}
