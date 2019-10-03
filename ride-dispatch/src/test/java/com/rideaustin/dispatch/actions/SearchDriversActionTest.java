package com.rideaustin.dispatch.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.StateMachinePersist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import com.rideaustin.StubStateContext;
import com.rideaustin.dispatch.tasks.PreauthorizationTask;
import com.rideaustin.model.Area;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.CityDriverType.DefaultDriverTypeConfiguration;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.ActiveDriverSearchCriteria;
import com.rideaustin.service.DefaultSearchDriverHandler;
import com.rideaustin.service.DirectConnectSearchDriverHandler;
import com.rideaustin.service.MapService;
import com.rideaustin.service.QueuedActiveDriverSearchCriteria;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.config.ActiveDriverServiceConfig;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.ride.DriverTypeService;
import com.rideaustin.utils.dispatch.StateMachineUtils;
import com.sromku.polygon.Polygon;

public class SearchDriversActionTest extends PersistingContextSupport {

  private ObjectMapper objectMapper = new ObjectMapper();
  @Mock
  private BeanFactory beanFactory;
  @Mock
  private TaskScheduler taskScheduler;
  @Mock
  private DriverTypeService driverTypeService;
  @Mock
  private ActiveDriverServiceConfig searchConfig;
  @Mock
  private AreaService areaService;
  @Mock
  private RideDispatchServiceConfig config;
  @Mock
  private StateMachinePersist<States, Events, String> access;
  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  private StackedDriverRegistry stackedDriverRegistry;
  @Mock
  private MapService mapService;
  @Mock
  private ActiveDriverLocationService activeDriverLocationService;
  @Mock
  private RidePaymentConfig ridePaymentConfig;
  @Mock
  private DefaultSearchDriverHandler defaultSearchDriverHandler;
  @Mock
  private DirectConnectSearchDriverHandler directConnectSearchDriverHandler;
  @Mock
  private PreauthorizationTask preauthorizationTask;
  @Mock
  private Polygon cityCenterPolygon;

  @InjectMocks
  private SearchDriversAction testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new SearchDriversAction();
    testedInstance.setObjectMapper(objectMapper);

    MockitoAnnotations.initMocks(this);
    when(beanFactory.getBean(DefaultSearchDriverHandler.class)).thenReturn(defaultSearchDriverHandler);
    when(beanFactory.getBean(DirectConnectSearchDriverHandler.class)).thenReturn(directConnectSearchDriverHandler);
    requestContext.setDriverSearchRadius(5);
    when(config.getDriverSearchRadiusLimit()).thenReturn(50);
    when(config.getCityCenterDispatchPolygon()).thenReturn(cityCenterPolygon);
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
  }

  @Test
  public void executeAbortsWhenRideIsAlreadyBeingDispatched() {
    final long rideId = 1L;
    requestContext.setRideId(rideId);
    requestContext.setCityId(1L);
    dispatchContext.setCandidate(new DispatchCandidate());
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);

    testedInstance.execute(context);

    verify(config, never()).getTotalDispatchWaitTime(requestContext.getCityId());
  }

  @Test
  public void executeSendsNADOnExpiredDispatch() {
    final long rideId = 1L;
    requestContext.setRideId(rideId);
    requestContext.setCityId(1L);
    requestContext.setCreatedDate(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)));
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
    when(config.getTotalDispatchWaitTime(anyLong())).thenReturn(10);

    testedInstance.execute(context);

    assertEquals(Events.NO_DRIVERS_AVAILABLE, ((StubStateContext.StubStateMachine) context.getStateMachine()).getLastSentEvent());
  }

  @Test
  public void executeResolvesTopLevelArea() {
    final long rideId = 1L;
    final double startLocationLat = 34.681681;
    final double startLocationLong = -97.896161;
    final long cityId = 1L;
    requestContext.setRideId(rideId);
    requestContext.setCityId(cityId);
    requestContext.setCreatedDate(Date.from(Instant.now().minus(1, ChronoUnit.SECONDS)));
    requestContext.setStartLocationLat(startLocationLat);
    requestContext.setStartLocationLong(startLocationLong);
    requestContext.setIgnoreIds(new HashSet<>());
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
    when(config.getTotalDispatchWaitTime(anyLong())).thenReturn(10);
    when(driverTypeService.getCityDriverType(anyInt(), eq(cityId))).thenReturn(Optional.empty());
    final long parentAreaId = 1L;
    final Area area = new Area();
    area.setVisibleToDrivers(false);
    area.setParentAreaId(parentAreaId);
    when(areaService.isInArea(any(LatLng.class), eq(cityId))).thenReturn(area);
    when(config.getDriverSearchRadiusLimit()).thenReturn(0);

    testedInstance.execute(context);

    verify(areaService).getById(eq(parentAreaId));
  }

  @Test
  public void executeSearchesDriversInQueueWhenRequestIsInArea() {
    final long rideId = 1L;
    final double startLocationLat = 34.681681;
    final double startLocationLong = -97.896161;
    final long cityId = 1L;
    requestContext.setRideId(rideId);
    requestContext.setCityId(cityId);
    requestContext.setCreatedDate(Date.from(Instant.now().minus(1, ChronoUnit.SECONDS)));
    requestContext.setStartLocationLat(startLocationLat);
    requestContext.setStartLocationLong(startLocationLong);
    requestContext.setIgnoreIds(new HashSet<>());
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
    when(config.getTotalDispatchWaitTime(anyLong())).thenReturn(10);
    when(driverTypeService.getCityDriverType(anyInt(), eq(cityId))).thenReturn(Optional.empty());
    final Area area = new Area();
    when(areaService.isInArea(any(LatLng.class), eq(cityId))).thenReturn(area);
    when(config.getDriverSearchRadiusLimit()).thenReturn(0);

    testedInstance.execute(context);

    verify(defaultSearchDriverHandler).searchDrivers(argThat(new BaseMatcher<QueuedActiveDriverSearchCriteria>(){

      @Override
      public boolean matches(Object o) {
        final QueuedActiveDriverSearchCriteria criteria = (QueuedActiveDriverSearchCriteria) o;
        return criteria.getArea().equals(area);
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }

  @Test
  public void executeStartPreauthTask() {
    final long rideId = 1L;
    final double startLocationLat = 34.681681;
    final double startLocationLong = -97.896161;
    final long cityId = 1L;
    requestContext.setRideId(rideId);
    requestContext.setCityId(cityId);
    requestContext.setCreatedDate(Date.from(Instant.now().minus(1, ChronoUnit.SECONDS)));
    requestContext.setStartLocationLat(startLocationLat);
    requestContext.setStartLocationLong(startLocationLong);
    requestContext.setIgnoreIds(new HashSet<>());
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
    when(config.getTotalDispatchWaitTime(anyLong())).thenReturn(10);
    when(driverTypeService.getCityDriverType(anyInt(), eq(cityId))).thenReturn(Optional.empty());
    final Area area = new Area();
    when(areaService.isInArea(any(LatLng.class), eq(cityId))).thenReturn(area);
    when(ridePaymentConfig.isAsyncPreauthEnabled()).thenReturn(true);
    when(beanFactory.getBean(PreauthorizationTask.class)).thenReturn(preauthorizationTask);
    when(preauthorizationTask.withRideId(anyLong())).thenAnswer((Answer<PreauthorizationTask>) invocation -> (PreauthorizationTask) invocation.getMock());
    when(preauthorizationTask.withApplePayToken(anyString())).thenAnswer((Answer<PreauthorizationTask>) invocation -> (PreauthorizationTask) invocation.getMock());
    final OnlineDriverDto driver = new OnlineDriverDto();
    driver.setLocationObject(new LocationObject(34.068161, -97.8918961));
    when(defaultSearchDriverHandler.searchDrivers(any(QueuedActiveDriverSearchCriteria.class))).thenReturn(
      new ArrayList<>(Collections.singletonList(driver))
    );
    when(activeDriverDslRepository.findDispatchCandidate(anyLong())).thenReturn(new DispatchCandidate());

    testedInstance.execute(context);

    verify(taskScheduler).schedule(eq(preauthorizationTask), any(Date.class));
  }

  @Test
  public void executeResolvesDefaultSearchHandlerWhenConfigurationInvalid() {
    final long rideId = 1L;
    final double startLocationLat = 34.681681;
    final double startLocationLong = -97.896161;
    final long cityId = 1L;
    requestContext.setRideId(rideId);
    requestContext.setCityId(cityId);
    requestContext.setCreatedDate(Date.from(Instant.now().minus(1, ChronoUnit.SECONDS)));
    requestContext.setStartLocationLat(startLocationLat);
    requestContext.setStartLocationLong(startLocationLong);
    requestContext.setIgnoreIds(new HashSet<>());
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
    when(config.getTotalDispatchWaitTime(anyLong())).thenReturn(10);
    when(config.getDriverSearchRadiusStep()).thenReturn(10);
    final CityDriverType cityDriverType = new CityDriverType();
    cityDriverType.setConfiguration("{\"searchHandlerClass\":\"foo\"}");
    cityDriverType.setConfigurationClass(DefaultDriverTypeConfiguration.class);
    when(driverTypeService.getCityDriverType(anyInt(), eq(cityId))).thenReturn(Optional.of(cityDriverType));

    testedInstance.execute(context);

    verify(defaultSearchDriverHandler, atLeastOnce()).searchDrivers(any(ActiveDriverSearchCriteria.class));
  }
}