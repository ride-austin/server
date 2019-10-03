package com.rideaustin.service.areaqueue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import com.rideaustin.model.Area;
import com.rideaustin.model.AreaQueueEntry;
import com.rideaustin.model.LocationAware;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.AreaDslRepository;
import com.rideaustin.repo.dsl.AreaQueueEntryDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.config.AreaQueueConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.user.CarTypesCache;

public class AreaQueueUpdateServiceTest {

  private static final String REGULAR = "REGULAR";

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
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  private AreaQueueEntryDslRepository areaQueueEntryDslRepository;
  @Mock
  private EventsNotificationService notificationService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private AreaDslRepository areaDslRepository;
  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private TimeService timeService;
  @Mock
  private ActiveDriverLocationService activeDriverLocationService;
  @Mock
  private ApplicationEventPublisher publisher;

  @Mock
  private AreaQueuePenaltyService penaltyService;

  @Mock
  private AreaQueueConfig config;

  private AreaQueueUpdateService areaQueueService;

  private List<Area> areas = new ArrayList<>();
  private Area country = new Area();
  private Area a = new Area();

  List<AreaQueueEntry> entries = new ArrayList<>();
  AreaQueueEntry entry = new AreaQueueEntry();
  ActiveDriver ad = new ActiveDriver();

  @Before
  public void setup() throws Exception {

    MockitoAnnotations.initMocks(this);

    AreaGeometry areaGeometry = new AreaGeometry();
    areaGeometry.setCsvGeometry("14.3200360,54.6181640 14.3200360,48.9513665 23.8856970,48.9513665 23.8856970,54.6181640");
    country.setAreaGeometry(areaGeometry);
    country.setId(1L);
    country.setName("Country");

    AreaGeometry areaGeometry1 = new AreaGeometry();
    areaGeometry1.setCsvGeometry("1,1 1,10 10,10 10,1");
    a.setAreaGeometry(areaGeometry1);
    a.setId(2L);
    a.setName("A Area");
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

    areaQueueService = new AreaQueueUpdateService(areaService, areaQueueEntryService,
      notificationService, timeService, activeDriverLocationService, penaltyService,
      activeDriverDslRepository, rideDslRepository, areaDslRepository, areaQueueEntryDslRepository,
      carTypesCache, config, publisher);

  }

  @Test
  public void testUpdateAreaQueuesStatusesNoEntries() throws RideAustinException {
    areaQueueService.updateStatuses(1L);

    verify(areaService, times(1)).getById(any());
    verify(areaQueueEntryService, times(1)).getEntries(any());
  }

  @Test
  public void testUpdateAreaQueuesStatusesInactiveDriver() throws RideAustinException {
    ad.setStatus(ActiveDriverStatus.AWAY);
    entry.setActiveDriver(ad);

    when(areaQueueEntryService.getEntries(any())).thenReturn(entries);
    when(rideDslRepository.findActiveByActiveDriver(any(ActiveDriver.class))).thenReturn(new Ride());

    areaQueueService.updateStatuses(1L);

    verify(areaService, times(1)).getById(any());
    verify(areaQueueEntryService, times(1)).getEntries(any());
  }

  @Test
  public void shouldNotifyInactiveDriverOnLeavingArea() throws RideAustinException, ParseException {
    // given
    ad.setStatus(ActiveDriverStatus.INACTIVE);
    ad.setInactiveOn(DATE_FORMATTER.parse(DATE1));
    entry.setActiveDriver(ad);
    when(areaQueueEntryService.getEntries(any())).thenReturn(entries);

    // when
    areaQueueService.updateStatuses(1L);

    // then
    verify(areaService, times(1)).getById(any());
    verify(areaQueueEntryService, times(1)).getEntries(any());
    verify(notificationService, times(1)).sendQueuedAreaGoingInactiveToDriver(anyLong(), any());
  }

  @Test
  public void shouldWaitWithNotifyingInactiveDriverOnLeavingArea() throws RideAustinException, ParseException {
    // given
    ad.setStatus(ActiveDriverStatus.INACTIVE);
    ad.setInactiveOn(DATE_FORMATTER.parse(DATE2));
    entry.setActiveDriver(ad);
    when(areaQueueEntryService.getEntries(any())).thenReturn(entries);

    // when
    areaQueueService.updateStatuses(1L);

    // then
    verify(areaService, times(1)).getById(any());
    verify(areaQueueEntryService, times(1)).getEntries(any());
    verify(notificationService, times(0)).sendQueuedAreaGoingInactiveToDriver(anyLong(), any());
  }

  @Test
  public void testUpdateAreaQueuesActiveDriverOutsideAreaDontRemoveEntry() throws RideAustinException {

    when(areaQueueEntryService.getEntries(any())).thenReturn(entries);
    areaQueueService.updateStatuses(1L);

    verify(areaService, times(1)).getById(any());
    verify(areaQueueEntryService, times(1)).getEntries(any());
    verify(areaService, times(1)).isInArea(any(LocationAware.class), anyLong());
  }

  @Test
  public void testUpdateAreaQueuesActiveDriverOutsideAreaRemoveEntry() throws RideAustinException, ParseException {

    when(areaQueueEntryService.getEntries(any())).thenReturn(entries);
    entries.get(0).setLastPresentInQueue(DATE_FORMATTER.parse(DATE1));
    areaQueueService.updateStatuses(1L);

    verify(areaService, times(1)).getById(any());
    verify(areaQueueEntryService, times(1)).getEntries(any());
    verify(areaService, times(1)).isInArea(any(LocationAware.class), anyLong());
  }

  @Test
  public void testUpdateAreaQueuesActiveDriverInSameArea() throws RideAustinException {

    when(areaService.getById(any())).thenReturn(country);
    when(areaService.getAllAreas()).thenReturn(areas);
    when(areaService.isInArea(any(LocationAware.class), anyLong())).thenReturn(country);
    when(areaQueueEntryService.getEntries(any())).thenReturn(entries);

    areaQueueService.updateStatuses(1L);

    verify(areaService, times(1)).getById(any());
    verify(areaQueueEntryService, times(1)).getEntries(any());
    verify(areaService, times(1)).isInArea(any(LocationAware.class), anyLong());
  }
}