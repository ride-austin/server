package com.rideaustin.service.rating;

import java.util.Map;
import java.util.Optional;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractRatingService<T> implements RatingService<T> {

  public static final String RATING_CONFIG = "ratingConfiguration";
  public static final String MINIMUM_RATING_THRESHOLD_KEY = "minimumRatingThreshold";
  public static final String DEFAULT_RATING_KEY = "defaultRating";
  public static final String LIMIT_KEY = "limit";
  private static final int DEFAULT_RATING_RIDES = 5;
  private static final double DEFAULT_RATING = 5d;
  private static final Integer DEFAULT_LIMIT = 50;

  @NonNull
  private final ConfigurationItemCache configCache;
  @NonNull
  private ClientType clientType;

  protected Integer getMinimumRatingThreshold() {
    Optional<ConfigurationItem> ratingConfig = getRatingConfig();
    return ratingConfig
      .map(configurationItem -> (Integer) ((Map) configurationItem.getConfigurationObject()).get(MINIMUM_RATING_THRESHOLD_KEY))
      .orElse(DEFAULT_RATING_RIDES);
  }

  public Double getDefaultRating() {
    Optional<ConfigurationItem> ratingConfig = getRatingConfig();
    return ratingConfig
      .map(configurationItem -> (Double) ((Map) configurationItem.getConfigurationObject()).get(DEFAULT_RATING_KEY))
      .orElse(DEFAULT_RATING);
  }

  public Integer getLimit() {
    Optional<ConfigurationItem> ratingConfig = getRatingConfig();
    return ratingConfig
      .map(configurationItem -> (Integer) ((Map) configurationItem.getConfigurationObject()).get(LIMIT_KEY))
      .orElse(DEFAULT_LIMIT);
  }

  private Optional<ConfigurationItem> getRatingConfig() {
    return configCache.getConfigurationForClient(clientType)
        .stream()
        .filter(ci -> RATING_CONFIG.equals(ci.getConfigurationKey()))
        .findFirst();
  }

}
