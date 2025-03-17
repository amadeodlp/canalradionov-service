package com.amadeodlp.canalradionov.core.services.media;

import com.amadeodlp.canalradionov.core.model.media.Episode;
import com.amadeodlp.canalradionov.core.model.media.RadioShow;

import java.util.List;

public interface MediaService {
    List<RadioShow> getAllShows();
    
    RadioShow getShowById(String id);
    
    List<RadioShow> getLiveShows();
    
    List<RadioShow> getUpcomingShows();
    
    Episode getEpisode(String showId, String episodeId);
    
    void incrementPlayCount(String showId, String episodeId);
    
    List<RadioShow> searchShows(String query);
    
    List<RadioShow> getFeaturedShows();
    
    List<RadioShow> getRecommendedShows();
}
