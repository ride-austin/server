package com.rideaustin.dispatch.actions;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideDriverDispatch;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class SendDispatchRequestActionTest extends PersistingContextSupport {

  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  private EventsNotificationService eventsNotificationService;
  @Mock
  private RideDispatchServiceConfig config;

  @InjectMocks
  private SendDispatchRequestAction testedInstance;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new SendDispatchRequestAction();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeSetsRequestedAtForCandidate() {
    final DispatchCandidate candidate = new DispatchCandidate();
    dispatchContext.setCandidate(candidate);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);

    testedInstance.execute(context);

    assertNotNull(candidate.getRequestedAt());
  }

  @Test
  public void executeSendsLPEventNotification() {
    final DispatchCandidate candidate = new DispatchCandidate();
    final long rideId = 1L;
    final long drivingTimeToRider = 100L;
    candidate.setDrivingTimeToRider(drivingTimeToRider);
    dispatchContext.setId(rideId);
    dispatchContext.setCandidate(candidate);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);
    final Ride ride = new Ride();
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    when(config.getRideRequestDeliveryTimeout()).thenReturn(10);

    testedInstance.execute(context);

    verify(eventsNotificationService).sendRideRequest(eq(ride), eq(candidate), eq(10000L), any(Date.class),
      any(Date.class), eq(drivingTimeToRider));
  }

  @Test
  public void executeTracksDispatch() {
    final DispatchCandidate candidate = new DispatchCandidate();
    final long rideId = 1L;
    final long drivingTimeToRider = 100L;
    final long drivingDistanceToRider = 200L;
    final double latitude = 34.084166;
    final double longitude = -97.4816168;
    candidate.setDrivingTimeToRider(drivingTimeToRider);
    candidate.setDrivingDistanceToRider(drivingDistanceToRider);
    candidate.setLatitude(latitude);
    candidate.setLongitude(longitude);
    dispatchContext.setId(rideId);
    dispatchContext.setCandidate(candidate);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);
    final Ride ride = new Ride();
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    verify(rideDriverDispatchDslRepository).save(argThat(new BaseMatcher<RideDriverDispatch>() {
      @Override
      public boolean matches(Object o) {
        final RideDriverDispatch dispatch = (RideDriverDispatch) o;
        return dispatch.getDispatchedOn() != null &&
          dispatch.getStatus() == DispatchStatus.DISPATCHED &&
          dispatch.getDrivingTimeToRider().equals(drivingTimeToRider) &&
          dispatch.getDrivingDistanceToRider().equals(drivingDistanceToRider) &&
          dispatch.getDispatchLocationLat().equals(latitude) &&
          dispatch.getDispatchLocationLong().equals(longitude);
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }

}