package com.rideaustin.utils.map;

import static com.rideaustin.utils.map.MapUtils.calculateDirectDistance;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import com.rideaustin.clients.configuration.ConfigurationItemService;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.City;
import com.rideaustin.rest.model.Location;
import com.rideaustin.service.CityService;
import com.rideaustin.utils.map.LocationCorrectorConfiguration.PickupHint;
import com.rideaustin.utils.map.LocationCorrectorConfiguration.PickupHint.DesignatedPickup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LocationCorrector {

  private final ConfigurationItemService configurationItemService;
  private final CityService cityService;
  private final ObjectMapper mapper;

  private final Map<City, LocationCorrectorConfiguration> configurationMap = new HashMap<>();

  public Optional<DesignatedPickup> correctLocation(@Nonnull LatLng location) {
    City closestCity = cityService.findClosestByCoordinates(new Location(location));
    LocationCorrectorConfiguration configuration = getConfiguration(closestCity);
    if (configuration != null) {
      for (PickupHint pickupHint : configuration.getPickupHints()) {
        if (pickupHint.getPolygon().contains(location.lat, location.lng)) {
          Optional<DesignatedPickup> nearestPickup = findNearest(pickupHint.getDesignatedPickups(), location);
          if (nearestPickup.isPresent()) {
            location.lat = nearestPickup.get().getPoint().lat;
            location.lng = nearestPickup.get().getPoint().lng;
          }
          return nearestPickup;
        }
      }
    }
    return Optional.empty();
  }

  private LocationCorrectorConfiguration getConfiguration(City closestCity) {
    LocationCorrectorConfiguration configuration = configurationMap.get(closestCity);
    if (configuration == null) {
      try {
        configuration = mapper.readValue(
          configurationItemService.findByKeyClientAndCity("geoCodingConfiguration", ClientType.RIDER,
            closestCity.getId()).getConfigurationValue(), LocationCorrectorConfiguration.class);
      } catch (IOException e) {
        log.error("Failed to read location corrector configuration", e);
        configuration = null;
      }
      configurationMap.put(closestCity, configuration);
    }
    return configuration;
  }

  @Nonnull
  private Optional<DesignatedPickup> findNearest(List<DesignatedPickup> designatedPickups, final LatLng location) {
    return designatedPickups.stream()
      .min(Comparator.comparingDouble(o -> calculateDirectDistance(o.getPoint().lng, o.getPoint().lat, location.lng, location.lat)));
  }

}
