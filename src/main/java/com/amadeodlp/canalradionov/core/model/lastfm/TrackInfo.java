package com.amadeodlp.canalradionov.core.model.lastfm;

/**
 * Model class representing track information from Last.fm API
 */
public record TrackInfo(
    String name,
    String artist,
    String album,
    String url,
    String imageUrl,
    int listeners,
    int playCount,
    String wiki,
    boolean isLoved,
    String[] tags
) {
}
