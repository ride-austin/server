package com.rideaustin.service.ride.eventshandlers;

import java.util.Date;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.RideTrackerService;
import com.rideaustin.service.ride.RideEventHandler;
import com.rideaustin.service.ride.events.UpdateRideLocationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UpdateRideLocationEventHandler implements RideEventHandler<UpdateRideLocationEvent> {

  private final RideTrackerService rideTrackerService;

  @Override
  public void handle(UpdateRideLocationEvent event) {
    RideTracker rideTracker = new RideTracker(event.getLatitude(), event.getLongitude(),
      event.getSpeed(), event.getHeading(), event.getCourse(), event.getTimestamp() / 1000);

    try {
      rideTrackerService.updateCachedRideTracker(event.getRideId(), rideTracker, new Date(event.getTimestamp()));
    } catch (RideAustinException e) {
      log.error("Error processing event - update location", e);
    }
  }
}
