package com.rideaustin.service.ride.events;

import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.rideaustin.service.ride.RideEvent;

import lombok.Getter;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Getter
public class UpdateRideLocationEvent extends RideEvent {

  private Double latitude;
  private Double longitude;
  private Double speed = null;
  private Double heading = null;
  private Double course = null;

  public UpdateRideLocationEvent(Map<String, String> eventProperties) {
    super(eventProperties);
    latitude = Double.parseDouble(eventProperties.get("latitude"));
    longitude = Double.parseDouble(eventProperties.get("longitude"));
    if (eventProperties.get("speed") != null) {
      speed = Double.parseDouble(eventProperties.get("speed"));
    }
    if (eventProperties.get("heading") != null) {
      heading = Double.parseDouble(eventProperties.get("heading"));
    }
    if (eventProperties.get("course") != null) {
      course = Double.parseDouble(eventProperties.get("course"));
    }
  }

  @Override
  public String toString() {
    return String.format("UpdateRideLocationEvent{latitude=%s, longitude=%s, speed=%s, heading=%s, course=%s} - %s", latitude, longitude, speed, heading, course, super.toString());
  }
}
