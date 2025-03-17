package com.amadeodlp.canalradionov.core.model.radio;

import java.util.List;

/**
 * Model class representing a radio station from the Radio Browser API.
 */
public record RadioStation(
    String stationId,
    String name,
    String url,
    String urlResolved, 
    String homepage,
    String favicon,
    String tags,
    String country,
    String countryCode,
    String language,
    String codec,
    int bitrate,
    boolean isHttps,
    int votes,
    int clickCount,
    String genre,
    boolean isOnline,
    int clickTrend,
    List<String> genreList,
    String streamType
) {
    // Constructs a trimmed version of the station for client responses
    public RadioStationDto toDto() {
        return new RadioStationDto(
            stationId,
            name,
            urlResolved, // Using the resolved URL for playback
            favicon,
            countryCode,
            language,
            codec,
            bitrate,
            isHttps,
            String.join(", ", genreList),
            isOnline
        );
    }
}
