package com.rideaustin.dispatch.actions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.rideaustin.model.Address;
import com.rideaustin.rest.model.RideLocation;
import com.rideaustin.service.MapService;

import lombok.extern.slf4j.Slf4j;

public interface AddressAwareAction {

  @Nullable
  default Address getAddress(@Nonnull RideLocation location, MapService mapService) {
    if (StringUtils.isNotBlank(location.getAddress())) {
      return new Address(location.getAddress(), location.getZipCode());
    } else {
      try {
        return mapService.reverseGeocodeAddress(location.getLat(), location.getLng());
      } catch (Exception e) {
        LoggingFacility.log.error("Failed to reverse geocode", e);
        return null;
      }
    }
  }

  @Slf4j
  class LoggingFacility {
  }
}
