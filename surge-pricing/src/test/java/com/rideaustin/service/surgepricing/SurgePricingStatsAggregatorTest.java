package com.rideaustin.service.surgepricing;

import static com.rideaustin.service.SurgePricingTestUtils.ZIP_CODE;
import static com.rideaustin.service.SurgePricingTestUtils.mockSurgeArea;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rideaustin.model.Address;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.redis.AreaGeometry;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.repo.redis.SurgeAreaRedisRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.GeolocationLogService;
import com.rideaustin.service.user.CarTypesUtils;
import com.rideaustin.utils.GeometryUtils;
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GeometryUtils.class, CarTypesUtils.class})
public class SurgePricingStatsAggregatorTest {

  private static final long AREA_ID = 1L;
  private static final long AREA1_ID = 2L;

  private static final Polygon polygon = Polygon.Builder()
    .addVertex(new Point(-1, 1))
    .addVertex(new Point(-1, -1))
    .addVertex(new Point(1, -1))
    .addVertex(new Point(-11, 1))
    .build();

  @Mock
  private SurgeAreaDslRepository surgeAreaDslRepository;
  @Mock
  private GeolocationLogService geolocationLogService;
  @Mock
  private SurgeAreaRedisRepository surgeAreaRedisRepository;
  @Mock
  private SurgePricingService surgePricingService;
  @Mock
  private ActiveDriverLocationService activeDriverLocationService;

  private SurgePricingStatsAggregator testedInstance;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testedInstance = new SurgePricingStatsAggregator(surgeAreaDslRepository, surgeAreaRedisRepository, surgePricingService,
      geolocationLogService, activeDriverLocationService);

    AreaGeometry areaGeometry = new AreaGeometry();

    areaGeometry.setId(AREA_ID);

    SurgeArea surgeArea = mockSurgeArea(BigDecimal.valueOf(1.5d));
    surgeArea.setName(ZIP_CODE);

    PowerMockito.mockStatic(GeometryUtils.class, CarTypesUtils.class);
    when(GeometryUtils.buildPolygon(anyString())).thenReturn(polygon);
    when(CarTypesUtils.fromBitMask(anyInt())).thenReturn(Collections.singleton("REGULAR"));
  }

  @Test
  public void testRecalculateSurgeAreas() throws BadRequestException {
    //given
    List<Ride> rides = Lists.newArrayList(
      mockRide(RideStatus.NO_AVAILABLE_DRIVER, AREA_ID),
      mockRide(RideStatus.COMPLETED, AREA_ID),
      mockRide(RideStatus.REQUESTED, AREA1_ID),
      mockRide(RideStatus.ACTIVE, AREA1_ID));

    List<RedisSurgeArea> surgeAreas = mockSurgeAreas();

    SurgeArea existing = mockSurgeArea(BigDecimal.ONE);
    when(surgeAreaDslRepository.findOne(anyLong())).thenReturn(existing);

    //when
    List<RedisSurgeArea> saved = recalculateSurgeAreas(rides, surgeAreas);

    //then
    assertThat(saved.get(1).getNumberOfAcceptedRides().get("REGULAR"), equalTo(1));
    assertThat(saved.get(1).getNumberOfRequestedRides().get("REGULAR"), equalTo(1));
  }

  @Test
  public void testRecalculateSurgeAreasNoRides() throws BadRequestException {
    List<Ride> rides = Lists.newArrayList();

    List<RedisSurgeArea> surgeAreas = mockSurgeAreas();
    surgeAreas.get(0).setNumberOfAcceptedRides(ImmutableMap.of("REGULAR", 10));
    surgeAreas.get(0).setNumberOfRequestedRides(ImmutableMap.of("REGULAR", 10));
    surgeAreas.get(1).setNumberOfAcceptedRides(ImmutableMap.of("REGULAR", 10));
    surgeAreas.get(1).setNumberOfRequestedRides(ImmutableMap.of("REGULAR", 10));

    SurgeArea existing = mockSurgeArea(BigDecimal.ONE);
    when(surgeAreaDslRepository.findOne(anyLong())).thenReturn(existing);

    List<RedisSurgeArea> saved = recalculateSurgeAreas(rides, surgeAreas);

    assertThat(saved.get(1).getNumberOfAcceptedRides().get("REGULAR"), equalTo(0));
    assertThat(saved.get(1).getNumberOfRequestedRides().get("REGULAR"), equalTo(0));
  }

  private List<RedisSurgeArea> recalculateSurgeAreas(List<Ride> rides, List<RedisSurgeArea> surgeAreas) {
    when(surgeAreaDslRepository.findSurgeAreaRides(anyLong(), anyObject(), anySetOf(RideStatus.class))).thenReturn(rides);
    when(surgeAreaRedisRepository.findByCityId(anyLong())).thenReturn(surgeAreas);
    return testedInstance.updateSurgeAreasStats(Date.from(Instant.now()), Date.from(Instant.now()), 1L);
  }

  private Ride mockRide(RideStatus status, long startAreaId) {
    Ride ride = new Ride();
    Address address = new Address();
    ride.setStart(address);
    ride.setStatus(status);
    ride.setStartAreaId(startAreaId);
    ride.setRequestedCarType(new CarType("REGULAR", "", "", "", "","", "", "", "", "", null, 4, 1, 1, true, Collections.emptySet()));
    return ride;
  }

  private List<RedisSurgeArea> mockSurgeAreas() {

    RedisSurgeArea s1 = new RedisSurgeArea();
    s1.setId(1L);
    s1.setCarCategoriesNumberOfEyeballs(mockEyeballs("REGULAR", 10));
    s1.setCityId(1L);

    RedisSurgeArea s2 = new RedisSurgeArea();
    s2.setId(2L);
    s2.setCarCategoriesNumberOfEyeballs(mockEyeballs("REGULAR", 10));
    s2.setCityId(1L);

    List<RedisSurgeArea> surgeAreas = Lists.newArrayList(s1, s2);
    AreaGeometry geometry = new AreaGeometry();
    geometry.setId(AREA_ID);
    surgeAreas.get(0).setSurgeMapping(ImmutableMap.of("REGULAR", BigDecimal.ONE));
    surgeAreas.get(0).setAreaGeometry(geometry);

    AreaGeometry geometry1 = new AreaGeometry();
    geometry1.setId(AREA1_ID);
    surgeAreas.get(1).setSurgeMapping(ImmutableMap.of("REGULAR", BigDecimal.ONE));
    surgeAreas.get(1).setAreaGeometry(geometry1);

    return surgeAreas;
  }

  private Map<String, Integer> mockEyeballs(String carCategory, int count) {
    Map<String, Integer> eb = new HashMap<>();
    eb.put(carCategory, count);
    return eb;
  }

}