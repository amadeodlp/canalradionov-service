package com.amadeodlp.canalradionov.core.model.radio;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter parameters for radio station search
 */
public record RadioFilter(
    String name,
    String country,
    String countryCode,
    List<String> genres,
    String language,
    Boolean musicOnly,
    Boolean onlyOnline,
    Integer limit,
    Integer offset,
    String order,
    Boolean reverse
) {
    public RadioFilter {
        // Default values if not provided
        if (limit == null) limit = 100;
        if (offset == null) offset = 0;
        if (order == null) order = "votes";
        if (reverse == null) reverse = true;
        if (onlyOnline == null) onlyOnline = true;
        if (musicOnly == null) musicOnly = true;
        if (genres == null) genres = new ArrayList<>();
    }
    
    /**
     * Converts filter to query parameters for the API call
     */
    public String toQueryParameters() {
        StringBuilder sb = new StringBuilder();
        
        if (name != null && !name.isEmpty()) {
            sb.append("&name=").append(name);
        }
        
        if (country != null && !country.isEmpty()) {
            sb.append("&country=").append(country);
        }
        
        if (countryCode != null && !countryCode.isEmpty()) {
            sb.append("&countryCode=").append(countryCode);
        }
        
        if (language != null && !language.isEmpty()) {
            sb.append("&language=").append(language);
        }
        
        // Only add genres filter if we have genres and musicOnly is true
        if (musicOnly && !genres.isEmpty()) {
            sb.append("&tagList=").append(String.join(",", genres));
        } else if (musicOnly) {
            // If no specific genres but we want music only, add music-related genres
            sb.append("&tagList=music,songs,pop,rock,jazz,classical");
        }
        
        if (onlyOnline) {
            sb.append("&hidebroken=true");
        }
        
        sb.append("&limit=").append(limit);
        sb.append("&offset=").append(offset);
        sb.append("&order=").append(order);
        sb.append("&reverse=").append(reverse);
        
        return sb.toString();
    }
}
