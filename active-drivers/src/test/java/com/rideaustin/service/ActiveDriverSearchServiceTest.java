package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.rest.model.CompactActiveDriverDto;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.config.ActiveDriverServiceConfig;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesCache;
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;

public class ActiveDriverSearchServiceTest {

  private static final long DEFAULT_TIME_TO_RIDE = 20L;
  private static final String REGULAR = "REGULAR";
  private static final int COUNT_OF_CREATED_MOCKED_ACTIVE_DRIVERS = 20;

  @Mock
  private ActiveDriverServiceConfig config;
  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private Polygon polygon;
  @Mock
  private ActiveDriverLocationService activeDriverLocationService;
  @Mock
  private AreaService areaService;
  @Mock
  private UpdateDistanceTimeService updateDistanceTimeService;
  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;
  @Mock
  private DefaultSearchDriverHandler defaultSearchDriverHandler;

  private List<OnlineDriverDto> activeDriversSample = prepareListOfActiveDriverWithId();

  private ActiveDriverSearchService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(config.getNumberOfEtaDrivers()).thenReturn(3);
    when(config.getMaxActiveDriverAvatars()).thenReturn(20);
    when(config.getDriverMaxEtaTime()).thenReturn(21);
    when(config.getDriverCityCenterMaxEtaTime()).thenReturn(20);
    when(config.getCityCenterDispatchPolygon()).thenReturn(polygon);

    doAnswer(new MapServiceUpdateTimeToDriveAnswer(DEFAULT_TIME_TO_RIDE)).when(updateDistanceTimeService).updateDistanceTime(eq(10d), eq(10d), eq(activeDriversSample), anyInt(), anyBoolean());

    testedInstance = new ActiveDriverSearchService(config, activeDriverLocationService, areaService, updateDistanceTimeService,
      requestedDriversRegistry, defaultSearchDriverHandler);
  }

  @Test
  public void testFindAvailableActiveDriversForRiderFilterByAreaEta() {
    CarType carType = mockCarType();
    when(carTypesCache.getCarType(anyString())).thenReturn(carType);
    when(polygon.contains(any(Point.class))).thenReturn(true);

    doAnswer(new MapServiceUpdateTimeToDriveAnswer(21L)).when(updateDistanceTimeService).updateDistanceTime(eq(2.0d), eq(2.5d), anyListOf(OnlineDriverDto.class), anyInt(), anyBoolean());
    List<CompactActiveDriverDto> returnedDrivers = testedInstance
      .findAvailableActiveDriversForRider(2d, 2.5d, carType, null, 1L);

    assertThat(returnedDrivers.size(), is(Math.toIntExact(0)));
  }

  public class MapServiceUpdateTimeToDriveAnswer implements Answer {

    private Long drivingTimeToRider;

    public MapServiceUpdateTimeToDriveAnswer(Long drivingTimeToRider) {
      this.drivingTimeToRider = drivingTimeToRider;
    }

    @Override
    public List<OnlineDriverDto> answer(InvocationOnMock invocation) {
      List<OnlineDriverDto> arg = (List<OnlineDriverDto>) invocation.getArguments()[2];
      arg.forEach(ad -> ad.setDrivingTimeToRider(drivingTimeToRider));
      return arg;
    }
  }

  private List<OnlineDriverDto> prepareListOfActiveDriverWithId() {
    List<OnlineDriverDto> activeDrivers = new ArrayList<>();
    Random random = new Random();
    for (int i = 0; i < COUNT_OF_CREATED_MOCKED_ACTIVE_DRIVERS; i++) {
      OnlineDriverDto ad = new OnlineDriverDto(random.nextLong(), ActiveDriverStatus.AVAILABLE, 1L, 1L, 0, 0, 1, null, null, null, null);
      activeDrivers.add(ad);
    }
    return activeDrivers;
  }

  private CarType mockCarType() {
    CarType carType = new CarType();
    carType.setCarCategory(REGULAR);
    carType.setBitmask(1);
    return carType;
  }
}