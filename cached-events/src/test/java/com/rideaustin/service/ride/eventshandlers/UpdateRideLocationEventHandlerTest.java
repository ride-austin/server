package com.rideaustin.service.ride.eventshandlers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.service.RideTrackerService;
import com.rideaustin.service.ride.events.UpdateRideLocationEvent;

public class UpdateRideLocationEventHandlerTest {

  @Mock
  private RideTrackerService rideTrackerService;

  private UpdateRideLocationEventHandler testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new UpdateRideLocationEventHandler(rideTrackerService);
  }

  @Test
  public void handleUpdatesTrackers() throws BadRequestException {
    final double lat = 34.891981;
    final double lng = -97.4981891;
    final double speed = 64.0;
    final double course = 94.0;
    final double heading = 46.0;
    UpdateRideLocationEvent event = new UpdateRideLocationEvent(
      ImmutableMap.<String, String>builder()
        .put("rideId", "1")
        .put("eventTimestamp", String.valueOf(new Date().getTime()))
        .put("eventType", "UPDATE_LOCATION")
        .put("latitude", String.valueOf(lat))
        .put("longitude", String.valueOf(lng))
        .put("speed", String.valueOf(speed))
        .put("course", String.valueOf(course))
        .put("heading", String.valueOf(heading))
        .build()
    );

    testedInstance.handle(event);

    verify(rideTrackerService).updateCachedRideTracker(eq(1L), argThat(new BaseMatcher<RideTracker>() {
      @Override
      public boolean matches(Object o) {
        final RideTracker tracker = (RideTracker) o;
        return tracker.getLatitude().equals(lat) && tracker.getLongitude().equals(lng) && tracker.getCourse().equals(course)
          && tracker.getHeading().equals(heading) && tracker.getSpeed().equals(speed);
      }

      @Override
      public void describeTo(Description description) {

      }
    }), any(Date.class));
  }
}