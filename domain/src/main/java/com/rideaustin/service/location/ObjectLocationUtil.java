package com.rideaustin.service.location;

import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.OnlineDriverDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectLocationUtil {

  private static ObjectLocationService<OnlineDriverDto> objectLocationService;

  private ObjectLocationUtil() {}

  private static ObjectLocationService<OnlineDriverDto> getCacheInstance() {
    if (objectLocationService == null) {
      throw new IllegalStateException("Object location service error");
    }
    return objectLocationService;
  }

  public static void setObjectLocationService(ObjectLocationService<OnlineDriverDto> objectLocationService) {
    ObjectLocationUtil.objectLocationService = objectLocationService;
  }

  public static LocationObject get(Long ownerId, LocationType type) {
    try {
      return getCacheInstance().getById(ownerId, type).getLocationObject();
    } catch (Exception e) {
      log.error("Failed to get location info", e);
      return null;
    }
  }
}
