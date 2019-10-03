package com.rideaustin.service.areaqueue;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Area;
import com.rideaustin.model.LocationAware;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.AreaDslRepository;
import com.rideaustin.utils.GeometryUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AreaService {

  private final AreaCache areaCache;
  private final AreaDslRepository areaDslRepository;

  /**
   * Check, if ActiveDrivers  is inside one of our areas.
   * If yes - that area is returned
   * If not - null is returned
   */

  public Area isInArea(LocationAware activeDriver, final Long cityId) {
    return isInArea(new LatLng(activeDriver.getLatitude(), activeDriver.getLongitude()), cityId);
  }

  /**
   * Check, if Ride  is inside one of our areas.
   * If yes - that area is returned
   * If not - null is returned
   */
  public Area isInArea(Ride ride) {
    return isInArea(new LatLng(ride.getStartLocationLat(), ride.getStartLocationLong()), ride.getCityId());
  }

  public Area isInArea(@Nonnull LatLng location, @Nonnull Long cityId) {
    List<Area> areas = areaCache.getAreasPerCity(cityId);
    if (areas != null) {
      for (Area area : areas) {
        if (area.getAreaGeometry() != null && GeometryUtils.isInsideArea(area, location)) {
          return area;
        }
      }
    }
    return null;
  }

  public boolean isInExclusion(ActiveDriver activeDriver) {
    LatLng location = new LatLng(activeDriver.getLatitude(), activeDriver.getLongitude());
    Collection<Area> allAreas = getAllAreas();
    for (Area area : allAreas) {
      if (GeometryUtils.isInsideExclusions(area, location)) {
        return true;
      }
    }
    return false;
  }

  public Area getById(@Nonnull Long areaId) {
    return areaDslRepository.findOne(areaId);
  }

  public List<Area> getByKeys(@Nonnull String... keys) {
    return areaDslRepository.findByKeys(keys);
  }

  public List<Area> getAreasPerCity(Long cityId) {
    return areaCache.getAreasPerCity(cityId);
  }

  public Collection<Area> getAllAreas() {
    return areaCache.getAllAreas();
  }

}
