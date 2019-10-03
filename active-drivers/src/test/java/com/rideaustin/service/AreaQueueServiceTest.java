package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.Area;
import com.rideaustin.model.AreaQueueEntry;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.AreaQueueEntryDslRepository;
import com.rideaustin.rest.model.AreaQueuePositions;
import com.rideaustin.service.areaqueue.AreaQueueEntryService;
import com.rideaustin.service.areaqueue.AreaQueueService;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.config.AreaQueueConfig;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.user.CarTypesCache;

@RunWith(MockitoJUnitRunner.class)
public class AreaQueueServiceTest {

  private static final String REGULAR = "REGULAR";
  private static final String PREMIUM = "PREMIUM";

  private static final String DATE1 = "2016-01-01 12:00:00";
  private static final String DATE2 = "2016-01-01 12:01:00";
  private static final String DATE3 = "2016-01-01 12:02:01";
  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Mock
  private AreaService areaService;
  @Mock
  private AreaQueueEntryService areaQueueEntryService;
  @Mock
  private ActiveDriversService activeDriversService;
  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private TimeService timeService;
  @Mock
  private ActiveDriverLocationService activeDriverLocationService;
  @Mock
  private AreaQueueEntryDslRepository areaQueueEntryDslRepository;

  @Mock
  private AreaQueueConfig config;

  private AreaQueueService areaQueueService;

  private List<Area> areas = new ArrayList<>();
  private Area country = new Area();
  private Area a = new Area();

  List<AreaQueueEntry> entries = new ArrayList<>();
  AreaQueueEntry entry = new AreaQueueEntry();
  ActiveDriver ad = new ActiveDriver();

  @Before
  public void setup() throws Exception {

    AreaGeometry areaGeometry = new AreaGeometry();
    areaGeometry.setCsvGeometry("14.3200360,54.6181640 14.3200360,48.9513665 23.8856970,48.9513665 23.8856970,54.6181640");
    country.setAreaGeometry(areaGeometry);
    country.setId(1L);
    country.setName("Country");
    country.setVisibleToDrivers(true);

    AreaGeometry areaGeometry1 = new AreaGeometry();
    areaGeometry1.setCsvGeometry("1,1 1,10 10,10 10,1");
    a.setAreaGeometry(areaGeometry1);
    a.setId(2L);
    a.setName("A Area");
    a.setVisibleToDrivers(true);
    areas.add(a);
    areas.add(country);
    ad.setDriver(new Driver());

    ad.setStatus(ActiveDriverStatus.AVAILABLE);
    LocationObject locationObject = new LocationObject();
    locationObject.setLatitude(52.225678);
    locationObject.setLongitude(20.981819);
    ad.setLocationObject(locationObject);
    entry.setActiveDriver(ad);
    entry.setArea(country);
    entry.setCarCategory(REGULAR);
    entries.add(entry);

    when(areaService.getById(any())).thenReturn(country);
    when(areaService.getAllAreas()).thenReturn(areas);
    when(timeService.getCurrentDate()).thenReturn(DATE_FORMATTER.parse(DATE3));
    when(config.getOutOfAreaTimeThresholdBeforeLeave()).thenReturn(2);
    when(config.getInactiveTimeThresholdBeforeLeave()).thenReturn(2);

    areaQueueService = new AreaQueueService(areaService, areaQueueEntryService,
      activeDriversService, activeDriverLocationService, areaQueueEntryDslRepository, carTypesCache);

  }

  @Test
  public void getActiveAreaDetails() throws Exception {
    when(carTypesCache.getActiveCarTypes()).thenReturn(mockCarTypes());
    when(areaService.getAreasPerCity(1L)).thenReturn(areas);
    when(areaQueueEntryService.getEntries(any())).thenReturn(entries);
    List<AreaQueuePositions> areaQueueDetails = areaQueueService.getActiveAreaDetails(1L);

    assertThat(areaQueueDetails.get(0).getLengths().get(REGULAR), equalTo(1));
    assertThat(areaQueueDetails.get(0).getLengths().get(PREMIUM), equalTo(0));
    assertThat(areaQueueDetails.get(1).getLengths().get(REGULAR), equalTo(1));
    assertThat(areaQueueDetails.get(1).getLengths().get(PREMIUM), equalTo(0));
  }

  @Test
  public void getActiveAreaDetailsNoQueueInCityCity() throws Exception {
    List<AreaQueuePositions> areaQueueDetails = areaQueueService.getActiveAreaDetails(2L);
    assertThat(areaQueueDetails.size(), equalTo(0));
  }

  private Map<String, CarType> mockCarTypes() {
    CarType carType = new CarType();
    return ImmutableMap.<String, CarType>builder().
      put(REGULAR, carType).
      put(PREMIUM, carType).
      build();
  }

}
