package com.rideaustin.service.ride.events;

import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.service.ride.RideEvent;

import lombok.Getter;

@Getter
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EndRideEvent extends RideEvent {

  private RideEndLocation endLocation;

  public EndRideEvent(Map<String, String> eventProperties) {
    super(eventProperties);
    endLocation = new RideEndLocation();

    endLocation.setEndLocationLat(Double.valueOf(eventProperties.get("endLocationLat")));
    endLocation.setEndLocationLong(Double.valueOf(eventProperties.get("endLocationLong")));
    endLocation.setEndAddress(eventProperties.get("endAddress"));
    endLocation.setEndZipCode(eventProperties.get("endZipCode"));
  }

  @Override
  public String toString() {
    return "EndRideEvent{" +
      "endLocation=" + endLocation +","+
      super.toString()
      +"}";
  }
}
