package com.amadeodlp.canalradionov.core.model.radio;

/**
 * A lightweight version of RadioStation for client responses
 */
public record RadioStationDto(
    String stationId,
    String name,
    String streamUrl,
    String logoUrl,
    String countryCode,
    String language,
    String codec,
    int bitrate,
    boolean isSecure,
    String genres,
    boolean isOnline
) {
}
