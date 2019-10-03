package com.rideaustin.service.ride.events;

import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.rideaustin.service.ride.RideEvent;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DriverReachedEvent extends RideEvent {

  public DriverReachedEvent(Map<String, String> eventProperties) {
    super(eventProperties);
  }

}
