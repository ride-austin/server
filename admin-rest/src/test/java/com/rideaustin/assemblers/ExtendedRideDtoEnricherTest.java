package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.StubStateMachineContext;
import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.SessionDslRepository;
import com.rideaustin.rest.model.ExtendedRideDriverDto;
import com.rideaustin.rest.model.ExtendedRideDto;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;

public class ExtendedRideDtoEnricherTest {

  @Mock
  private Environment environment;
  @Mock
  private SessionDslRepository sessionDslRepository;
  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private ObjectLocationService<OnlineDriverDto> objectLocationService;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;

  private ExtendedRideDtoEnricher testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ExtendedRideDtoEnricher(environment, sessionDslRepository, contextAccess, objectLocationService, activeDriverDslRepository);
  }

  @Test
  public void enrichSkipsNull() {
    final ExtendedRideDto result = testedInstance.enrich(null);

    assertNull(result);
  }

  @Test
  public void enrichSetsStartedOn() throws Exception {
    ExtendedRideDto source = new ExtendedRideDto(1L, 1L, "A", "B", "REGULAR",
      null, null, null, RideStatus.ACTIVE, "1.0.0", "00000", "C",
      "00000", "D", null);
    final RideFlowContext rideFlowContext = new RideFlowContext();
    rideFlowContext.setStartedOn(new Date());
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of("flowContext", rideFlowContext)));

    final ExtendedRideDto result = testedInstance.enrich(source);

    assertNotNull(result.getStarted());
  }

  @Test
  public void enrichSetsDriverAppVersion() throws Exception {
    ExtendedRideDto source = new ExtendedRideDto(1L, 1L, "A", "B", "REGULAR",
      null, null, null, RideStatus.ACTIVE, "1.0.0", "00000", "C",
      "00000", "D", null);
    final RideFlowContext rideFlowContext = new RideFlowContext();
    final long sessionId = 1L;
    rideFlowContext.setDriverSession(sessionId);
    final Session session = new Session();
    session.setUserAgent("RideAustinDriver_1.0.0");
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of("flowContext", rideFlowContext)));
    when(sessionDslRepository.findOne(eq(sessionId))).thenReturn(session);

    final ExtendedRideDto result = testedInstance.enrich(source);

    assertEquals("1.0.0", result.getDriverAppVersion());
  }

  @Test
  public void enrichSetsOnlineDriverInfo() {
    final long activeDriverId = 1L;
    ExtendedRideDto source = new ExtendedRideDto(1L, 1L, "A", "B", "REGULAR",
      null, null, null, RideStatus.ACTIVE, "1.0.0", "00000", "C",
      "00000", "D", activeDriverId);
    final OnlineDriverDto onlineDriverDto = new OnlineDriverDto(activeDriverId, ActiveDriverStatus.RIDING, 1L, 1L, "A B", "+15555555555", "ABC");
    onlineDriverDto.setLocationObject(new LocationObject(34.0698168, -97.861616));
    when(objectLocationService.getById(eq(activeDriverId), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriverDto);

    final ExtendedRideDto result = testedInstance.enrich(source);

    assertEquals("A", result.getDriverFirstName());
    assertEquals("B", result.getDriverLastName());
    assertEquals(onlineDriverDto.getDriverId(), result.getDriverId().longValue());
    assertEquals(onlineDriverDto.getPhoneNumber(), result.getDriverPhoneNumber());
    assertEquals(onlineDriverDto.getLatitude(), result.getDriverLatitude(), 0.0);
    assertEquals(onlineDriverDto.getLongitude(), result.getDriverLongitude(), 0.0);
    assertEquals(activeDriverId, result.getActiveDriverId().longValue());

  }

  @Test
  public void enrichSetsDbDriverInfo() {
    final long activeDriverId = 1L;
    ExtendedRideDto source = new ExtendedRideDto(1L, 1L, "A", "B", "REGULAR",
      null, null, null, RideStatus.ACTIVE, "1.0.0", "00000", "C",
      "00000", "D", activeDriverId);
    final ExtendedRideDriverDto driverDto = new ExtendedRideDriverDto(1L, "A", "B", "+15555555555");
    when(activeDriverDslRepository.findExtendedRideDriverInfo(eq(activeDriverId))).thenReturn(driverDto);

    final ExtendedRideDto result = testedInstance.enrich(source);

    assertEquals(driverDto.getFirstName(), result.getDriverFirstName());
    assertEquals(driverDto.getLastName(), result.getDriverLastName());
    assertEquals(driverDto.getPhoneNumber(), result.getDriverPhoneNumber());
    assertEquals(driverDto.getDriverId(), result.getDriverId());
    assertEquals(activeDriverId, result.getActiveDriverId().longValue());
  }
}