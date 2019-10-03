package com.rideaustin.dispatch.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;

import com.rideaustin.dispatch.service.RideFlowStateMachineProvider;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.service.MapService;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.utils.dispatch.StateMachineUtils;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class UpdateDestinationActionTest extends PersistingContextSupport {

  @Mock
  private MapService mapService;
  @Mock
  private EventsNotificationService eventsNotificationService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private StackedDriverRegistry stackedDriverRegistry;
  @Mock
  private RideFlowStateMachineProvider machineProvider;

  @InjectMocks
  private UpdateDestinationAction testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new UpdateDestinationAction();

    MockitoAnnotations.initMocks(this);
  }

  @DataProvider
  public static Object[] ineligibleStatuses() {
    return EnumSet.complementOf(EnumSet.of(RideStatus.ACTIVE)).toArray();
  }

  @Test
  public void executeUpdatesEndLocation() {
    final long rideId = 1L;
    final RideEndLocation endLocation = new RideEndLocation();
    final double lat = 34.684681;
    final double lng = -97.1861968;
    endLocation.setEndLocationLat(lat);
    endLocation.setEndLocationLong(lng);
    context.addMessageHeader("endLocation", endLocation);
    context.getExtendedState().getVariables().put("rideId", rideId);
    final Ride ride = new Ride();
    when(rideDslRepository.findOne(rideId)).thenReturn(ride);

    testedInstance.execute(context);

    assertEquals(lat, ride.getEndLocationLat(), 0.0);
    assertEquals(lng, ride.getEndLocationLong(), 0.0);
  }

  @Test
  public void executeNotifiesDriver() {
    final long rideId = 1L;
    final RideEndLocation endLocation = new RideEndLocation();
    context.addMessageHeader("endLocation", endLocation);
    context.getExtendedState().getVariables().put("rideId", rideId);
    final Ride ride = new Ride();
    when(rideDslRepository.findOne(rideId)).thenReturn(ride);
    final DispatchCandidate candidate = new DispatchCandidate();
    dispatchContext.setCandidate(candidate);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);

    testedInstance.execute(context);

    verify(eventsNotificationService).sendRideUpdateToDriver(ride, dispatchContext.getCandidate(), EventType.END_LOCATION_UPDATED);
  }

  @Test
  public void executeIncreasesDestinationUpdatesCounter() {
    final long rideId = 1L;
    final RideEndLocation endLocation = new RideEndLocation();
    context.addMessageHeader("endLocation", endLocation);
    context.getExtendedState().getVariables().put("rideId", rideId);
    final Ride ride = new Ride();
    when(rideDslRepository.findOne(rideId)).thenReturn(ride);
    StateMachineUtils.updateFlowContext(context, flowContext, persister, environment);

    testedInstance.execute(context);

    assertEquals(1, flowContext.getDestinationUpdates());
  }

  @Test
  @UseDataProvider("ineligibleStatuses")
  public void executeDoesntUpdateNextRideInfoIfCurrentRideIsNotActive(RideStatus rideStatus) {
    final long rideId = 1L;
    final RideEndLocation endLocation = new RideEndLocation();
    context.addMessageHeader("endLocation", endLocation);
    context.getExtendedState().getVariables().put("rideId", rideId);
    final Ride ride = new Ride();
    ride.setStatus(rideStatus);
    when(rideDslRepository.findOne(rideId)).thenReturn(ride);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);

    testedInstance.execute(context);

    verify(rideDslRepository, never()).findNextRide(anyLong());
  }

  @Test
  public void executeDoesntUpdateNextRideInfoIfNoDispatchContext() {
    final long rideId = 1L;
    final RideEndLocation endLocation = new RideEndLocation();
    context.addMessageHeader("endLocation", endLocation);
    context.getExtendedState().getVariables().put("rideId", rideId);
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.ACTIVE);
    when(rideDslRepository.findOne(rideId)).thenReturn(ride);

    testedInstance.execute(context);

    verify(rideDslRepository, never()).findNextRide(anyLong());
  }

  @Test
  public void executeDoesntUpdateNextRideInfoIfNoDispatchCandidate() {
    final long rideId = 1L;
    final RideEndLocation endLocation = new RideEndLocation();
    context.addMessageHeader("endLocation", endLocation);
    context.getExtendedState().getVariables().put("rideId", rideId);
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.ACTIVE);
    when(rideDslRepository.findOne(rideId)).thenReturn(ride);
    dispatchContext.setCandidate(null);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);

    testedInstance.execute(context);

    verify(rideDslRepository, never()).findNextRide(anyLong());
  }

  @Test
  public void executeSendsForceRedispatchToNextRide() {
    final MobileDriverRideDto nextRide = mock(MobileDriverRideDto.class);
    final StateMachine nextRideMachine = mock(StateMachine.class);

    final long rideId = 1L;
    final RideEndLocation endLocation = new RideEndLocation();
    context.addMessageHeader("endLocation", endLocation);
    context.getExtendedState().getVariables().put("rideId", rideId);
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.ACTIVE);
    when(rideDslRepository.findOne(rideId)).thenReturn(ride);
    final DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(1L);
    dispatchContext.setCandidate(candidate);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);
    when(rideDslRepository.findNextRide(candidate.getId())).thenReturn(nextRide);
    when(stackedDriverRegistry.isStacked(candidate.getId())).thenReturn(true);
    when(machineProvider.restoreMachine(anyLong(), any(Events.class), any(MessageHeaders.class)))
      .thenReturn(Optional.of(nextRideMachine));
    when(nextRideMachine.sendEvent(any(Message.class))).thenReturn(true);

    testedInstance.execute(context);

    verify(nextRideMachine).sendEvent(argThat(new BaseMatcher<GenericMessage>() {
      @Override
      public boolean matches(Object o) {
        final GenericMessage message = (GenericMessage) o;
        return message.getPayload().equals(Events.FORCE_REDISPATCH);
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }
}