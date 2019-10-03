package com.rideaustin.service;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;

import com.google.common.collect.Lists;
import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideTrackerDslRepository;
import com.rideaustin.repo.jpa.ActiveDriverRepository;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.test.util.ActiveDriversUtils;
import com.rideaustin.utils.map.MapUtils;

@RunWith(PowerMockRunner.class)
public class RideTrackerServiceTest {

  private static final Double BASEPOS = 1.000d;
  private static final Double IS_NEAR = 1.004d;
  private static final Double DISTANT = 1.012d;
  private static final Double MIDDLE = (DISTANT + BASEPOS) / 2;
  private static final int SEC_20 = 20000;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private RideTrackerService rideTrackerService;

  @Mock
  private Environment environment;
  @Mock
  private ActiveDriverRepository activeDriverRepository;
  @Mock
  private RideDslRepository rideRepository;
  @Mock
  private RideTrackerDslRepository rideTrackerDslRepository;
  @Mock
  private MapService mapService;
  @Mock
  private S3StorageService s3StorageService;
  @Mock
  private TimeService timeService;

  @Before
  public void setup() throws Exception {
    ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setStatus(ActiveDriverStatus.RIDING);

    when(activeDriverRepository.findByUserAndNotInactiveStatus(anyObject())).thenReturn(activeDriver);

    Ride ride = new Ride();
    ride.setId(1L);
    ride.setStatus(RideStatus.ACTIVE);

    when(rideRepository.findActiveByActiveDriver(any(ActiveDriver.class))).thenReturn(ride);

    when(rideTrackerDslRepository.save(anyObject())).thenAnswer(invocation -> invocation.getArguments()[0]);

    when(timeService.getCurrentDate()).thenReturn(new Date());
    when(environment.acceptsProfiles(any())).thenReturn(true);
    rideTrackerService = new RideTrackerService(environment, mapService, s3StorageService, timeService, rideTrackerDslRepository, rideRepository);
  }

  @Test
  public void testEndRideWithCachedLocations() {
    List<RideTracker> trackers = ActiveDriversUtils.mockRideTrackers();

    when(rideTrackerDslRepository.findAllTrackerRecord(anyObject())).thenReturn(trackers);

    RideTracker mockedRideTracker = ActiveDriversUtils.mockRideTracker(10000L, 20d, 20d, 20d, 20d, 20d, new Date().getTime());

    trackers.add(mockedRideTracker);
    RideTracker endRideTracker = rideTrackerService.endRide(1L, mockedRideTracker);

    assertThat(endRideTracker.getDistanceTravelled(), is(nullValue()));
    assertThat(endRideTracker.getSequence(), is(not(nullValue())));
    assertThat(endRideTracker.getTrackedOn(), is(not(nullValue())));
    assertThat(endRideTracker.getRideId(), is(not(nullValue())));
  }

  @Test
  public void testEndRideWithHyperSpeed() {
    List<RideTracker> trackers = ActiveDriversUtils.mockRideTrackers();
    trackers.add(2, ActiveDriversUtils.mockRideTracker(1500L, 10d, 10d, 10d, 10d, 10d, new Date().getTime()));

    when(rideTrackerDslRepository.findAllTrackerRecord(anyObject())).thenReturn(trackers);

    RideTracker mockedRideTracker = ActiveDriversUtils.mockRideTracker(3000L, 20d, 20d, 20d, 20d, 20d, new Date().getTime());

    trackers.add(mockedRideTracker);
    RideTracker endRideTracker = rideTrackerService.endRide(1L, mockedRideTracker);

    assertThat(endRideTracker.getDistanceTravelled(), is(nullValue()));
    assertThat(endRideTracker.getSequence(), is(not(nullValue())));
    assertThat(endRideTracker.getTrackedOn(), is(not(nullValue())));
    assertThat(endRideTracker.getRideId(), is(not(nullValue())));
  }

  @Test
  public void testEndRideWithSameSequence() throws Exception {
    List<RideTracker> trackers = ActiveDriversUtils.mockRideTrackers();
    trackers.add(2, ActiveDriversUtils.mockRideTracker(1000L, 10d, 10d, 10d, 10d, 10d, new Date().getTime()));

    when(rideTrackerDslRepository.findAllTrackerRecord(anyObject())).thenReturn(trackers);

    RideTracker endRideTracker = rideTrackerService.endRide(1L, trackers.get(3));

    assertThat(trackers.get(2).getValid(), equalTo(Boolean.FALSE));
    assertThat(trackers.get(2).getDistanceTravelled(), is(nullValue()));
  }

  @Test
  public void testEndRideWithFilterHyperSpeedForEndTracker() {
    List<RideTracker> trackers = ActiveDriversUtils.mockRideTrackers();
    trackers.add(4, ActiveDriversUtils.mockRideTracker(3001L, 100d, 100d, 10d, 10d, 10d, new Date().getTime()));

    when(rideTrackerDslRepository.findAllTrackerRecord(anyObject())).thenReturn(trackers);

    RideTracker endRideTracker = rideTrackerService.endRide(1L, trackers.get(4));

    assertThat(endRideTracker.getValid(), equalTo(Boolean.FALSE));
    assertThat(endRideTracker.getDistanceTravelled(), is(nullValue()));
  }

  @Test
  public void testEndRideWithFilterHyperSpeedForEndTrackerTwoTrackers() {
    List<RideTracker> trackers = ActiveDriversUtils.mockRideTrackers(1);
    trackers.add(1, ActiveDriversUtils.mockRideTracker(3001L, 100d, 100d, 10d, 10d, 10d, new Date().getTime()));

    when(rideTrackerDslRepository.findAllTrackerRecord(anyObject())).thenReturn(trackers);

    RideTracker endRideTracker = rideTrackerService.endRide(1L, trackers.get(1));

    assertThat(endRideTracker.getValid(), equalTo(Boolean.TRUE));
    assertThat(endRideTracker.getDistanceTravelled(), is(not(nullValue())));
  }

  @Test
  public void testGetTrackersForRideReturnsEmptyListOnNull() {
    List<RideTracker> result = rideTrackerService.getTrackersForRide(null);

    assertEquals(0, result.size());
    verify(rideTrackerDslRepository, never()).findAllTrackerRecord(anyLong());
  }

  @Test
  public void testGetTrackersForRideCallsRideTrackerRepoOnNonNullRide() {
    rideTrackerService.getTrackersForRide(new Ride());

    verify(rideTrackerDslRepository, only()).findAllTrackerRecord(anyLong());
  }

  @Test
  public void shouldAddMissingPointsFromGoogleOnEndRide() {
    // given
    Calendar cal = getCalendar(2);
    Date startTime = cal.getTime();
    Date midTime = plusSeconds(cal, 60);
    Date endTime = plusSeconds(cal, 60);
    List<RideTracker> trackers = new ArrayList<>(Arrays.asList(
      RideTracker.builder().latitude(BASEPOS).longitude(BASEPOS).sequence(0L).trackedOn(startTime).valid(true).build(),
      RideTracker.builder().latitude(DISTANT).longitude(BASEPOS).sequence(toSec(midTime.getTime())).trackedOn(midTime).valid(true).build(),
      RideTracker.builder().latitude(DISTANT).longitude(DISTANT).sequence(Long.MAX_VALUE).trackedOn(endTime).valid(true).build()
    ));

    when(mapService.getGooglePointsBetween(BASEPOS, BASEPOS, DISTANT, BASEPOS)).thenReturn(Collections.singletonList(new LatLng(MIDDLE, MIDDLE)));
    when(mapService.getGooglePointsBetween(DISTANT, BASEPOS, DISTANT, DISTANT)).thenReturn(Arrays.asList(new LatLng(DISTANT, MIDDLE), new LatLng(MIDDLE, MIDDLE)));
    mockEndRideServices(trackers, endTime);

    // when
    RideTracker endRideTracker = rideTrackerService.endRide(1L, trackers.get(trackers.size() - 1));

    // then
    verify(mapService, times(2)).getGooglePointsBetween(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    verify(rideTrackerDslRepository, times(1)).saveAny(eq(endRideTracker));
    verify(rideTrackerDslRepository, times(1)).saveAnyMany(eq(trackers));
    assertEquals(6, trackers.size());
    assertTracker(endRideTracker, Long.MAX_VALUE, DISTANT, DISTANT, endTime);
    assertTracker(trackers.get(0), 0L, BASEPOS, BASEPOS, startTime);
    assertTracker(trackers.get(1), toSec(avgTime(startTime, midTime)), MIDDLE, MIDDLE, new Date(avgTime(startTime, midTime)));
    assertTracker(trackers.get(2), toSec(midTime.getTime()), DISTANT, BASEPOS, midTime);
    assertTracker(trackers.get(3), toSec(midTime.getTime() + SEC_20), DISTANT, MIDDLE, new Date(midTime.getTime() + SEC_20));
    assertTracker(trackers.get(4), toSec(midTime.getTime() + SEC_20 * 2), MIDDLE, MIDDLE, new Date(midTime.getTime() + SEC_20 * 2));
    assertEquals(trackers.get(5), endRideTracker);
  }

  @Test
  @PrepareForTest({RideTrackerService.class, MapUtils.class})
  public void shouldAddMissingPointsAndCalculateDistance() {
    // given
    Calendar cal = getCalendar(2);
    Date startTime = cal.getTime();
    Date midTime = plusSeconds(cal, 60);
    Date endTime = plusSeconds(cal, 60);
    List<RideTracker> trackers = new ArrayList<>(Arrays.asList(
      RideTracker.builder().latitude(BASEPOS).longitude(BASEPOS).sequence(0L).trackedOn(startTime).valid(true).build(),
      RideTracker.builder().latitude(DISTANT).longitude(BASEPOS).sequence(toSec(midTime.getTime())).trackedOn(midTime).valid(true).build(),
      RideTracker.builder().latitude(DISTANT).longitude(DISTANT).sequence(Long.MAX_VALUE).trackedOn(endTime).valid(true).build()
    ));

    when(mapService.getGooglePointsBetween(BASEPOS, BASEPOS, DISTANT, BASEPOS)).thenReturn(Collections.singletonList(new LatLng(MIDDLE, MIDDLE)));
    when(mapService.getGooglePointsBetween(DISTANT, BASEPOS, DISTANT, DISTANT)).thenReturn(Arrays.asList(new LatLng(DISTANT, MIDDLE), new LatLng(MIDDLE, MIDDLE)));

    when(rideTrackerDslRepository.findAllTrackerRecord(anyObject())).thenReturn(trackers);
    Double directDistance = 501d;

    PowerMockito.mockStatic(MapUtils.class);
    when(MapUtils.calculateDirectDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(directDistance);
    when(timeService.getCurrentDate()).thenReturn(endTime);
    // when
    RideTracker endRideTracker = rideTrackerService.endRide(1L, trackers.get(trackers.size() - 1));

    // then
    verify(mapService, times(2)).getGooglePointsBetween(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    verify(rideTrackerDslRepository, times(1)).saveAny(eq(endRideTracker));
    verify(rideTrackerDslRepository, times(1)).saveAnyMany(eq(trackers));
    assertEquals(6, trackers.size());
    for (int i = 0; i < 6; i++) {
      assertTrue(new BigDecimal(directDistance * i).compareTo(trackers.get(i).getDistanceTravelled()) == 0);
    }
    assertEquals(trackers.get(5), endRideTracker);
  }

  @Test
  public void shouldNotAddMissingPointsOnNoGoogleResult() {
    // given
    Calendar cal = getCalendar(2);
    Date startTime = cal.getTime();
    Date midTime = plusSeconds(cal, 60);
    Date endTime = plusSeconds(cal, 60);
    List<RideTracker> trackers = new ArrayList<>(Arrays.asList(
      RideTracker.builder().latitude(BASEPOS).longitude(BASEPOS).sequence(0L).trackedOn(startTime).valid(true).build(),
      RideTracker.builder().latitude(DISTANT).longitude(BASEPOS).sequence(toSec(midTime.getTime())).trackedOn(midTime).valid(true).build(),
      RideTracker.builder().latitude(DISTANT).longitude(DISTANT).sequence(Long.MAX_VALUE).trackedOn(endTime).valid(true).build()
    ));

    when(mapService.getGooglePointsBetween(BASEPOS, BASEPOS, DISTANT, BASEPOS)).thenReturn(Lists.newArrayList());
    when(mapService.getGooglePointsBetween(DISTANT, BASEPOS, DISTANT, DISTANT)).thenReturn(null);
    mockEndRideServices(trackers, endTime);

    // when
    RideTracker endRideTracker = rideTrackerService.endRide(1L, trackers.get(trackers.size() - 1));

    // then
    verify(mapService, times(2)).getGooglePointsBetween(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    verify(rideTrackerDslRepository, times(1)).saveAny(eq(endRideTracker));
    verify(rideTrackerDslRepository, times(1)).saveAnyMany(eq(trackers));
    assertEquals(3, trackers.size());
    assertTracker(trackers.get(0), 0L, BASEPOS, BASEPOS, startTime);
    assertTracker(trackers.get(1), toSec(midTime.getTime()), DISTANT, BASEPOS, midTime);
    assertTracker(endRideTracker, Long.MAX_VALUE, DISTANT, DISTANT, endTime);
    assertEquals(trackers.get(2), endRideTracker);
  }

  @Test
  public void shouldNotAddMissingPointsForShortDistance() {
    // given
    Calendar cal = getCalendar(2);
    Date startTime = cal.getTime();
    Date midTime = plusSeconds(cal, 60);
    Date endTime = plusSeconds(cal, 60);
    List<RideTracker> trackers = new ArrayList<>(Arrays.asList(
      RideTracker.builder().latitude(BASEPOS).longitude(BASEPOS).sequence(0L).trackedOn(startTime).valid(true).build(),
      RideTracker.builder().latitude(IS_NEAR).longitude(BASEPOS).sequence(toSec(midTime.getTime())).trackedOn(midTime).valid(true).build(),
      RideTracker.builder().latitude(IS_NEAR).longitude(IS_NEAR).sequence(Long.MAX_VALUE).trackedOn(endTime).valid(true).build()
    ));
    mockEndRideServices(trackers, trackers.get(2).getTrackedOn());

    // when
    RideTracker endRideTracker = rideTrackerService.endRide(1L, trackers.get(trackers.size() - 1));

    // then
    verify(mapService, never()).getGooglePointsBetween(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    verify(rideTrackerDslRepository, times(1)).saveAny(eq(endRideTracker));
    verify(rideTrackerDslRepository, times(1)).saveAnyMany(eq(trackers));
    assertEquals(3, trackers.size());
    assertTracker(trackers.get(0), 0L, BASEPOS, BASEPOS, startTime);
    assertTracker(trackers.get(1), toSec(midTime.getTime()), IS_NEAR, BASEPOS, midTime);
    assertTracker(endRideTracker, Long.MAX_VALUE, IS_NEAR, IS_NEAR, endTime);
    assertEquals(trackers.get(2), endRideTracker);
  }

  private void mockEndRideServices(List<RideTracker> trackers, Date trackedOn) {
    when(rideTrackerDslRepository.findAllTrackerRecord(anyObject())).thenReturn(trackers);
    when(timeService.getCurrentDate()).thenReturn(trackedOn);
  }

  private Calendar getCalendar(int minutesAgo) {
    Calendar time = Calendar.getInstance();
    time.add(Calendar.MINUTE, -minutesAgo);
    return time;
  }

  private long toSec(long time) {
    return time / 1000;
  }

  private Date plusSeconds(Calendar time, int seconds) {
    time.add(Calendar.SECOND, seconds);
    return time.getTime();
  }

  private long avgTime(Date t1, Date t2) {
    return (t1.getTime() + t2.getTime()) / 2;
  }

  private void assertTracker(RideTracker rideTracker, Long sequence, Double lat, Double lng, Date trackedOn) {
    assertEquals(sequence, rideTracker.getSequence());
    assertEquals(lat, rideTracker.getLatitude());
    assertEquals(lng, rideTracker.getLongitude());
    assertEquals(trackedOn.getTime(), rideTracker.getTrackedOn().getTime());
    assertEquals(true, rideTracker.getValid());
  }
}