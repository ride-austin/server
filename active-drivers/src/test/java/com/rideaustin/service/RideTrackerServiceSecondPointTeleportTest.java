package com.rideaustin.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideTrackerDslRepository;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.utils.DateUtils;

public class RideTrackerServiceSecondPointTeleportTest {

  private RideTrackerService testedInstance;

  @Mock
  private Environment environment;
  @Mock
  private MapService mapService;
  @Mock
  private RideDslRepository rideRepository;
  @Mock
  private S3StorageService s3StorageService;
  @Mock
  private RideTrackerDslRepository rideTrackerDslRepository;
  @Mock
  private TimeService timeService;
  @Captor
  private ArgumentCaptor<List<RideTracker>> savedTrackersCaptor;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new RideTrackerService(environment, mapService, s3StorageService, timeService, rideTrackerDslRepository, rideRepository);

    when(timeService.getCurrentDate()).thenCallRealMethod();
  }

  @Test
  public void testRecalculateWithTeleports() {
    when(rideTrackerDslRepository.findAllTrackerRecord(anyLong())).thenReturn(ImmutableList.of(
      RideTracker.builder()
        .latitude(30.240549).longitude(-97.785888)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017, 4, 23, 0, 44, 23), ZoneId.systemDefault()))
        .sequence(0L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.501611).longitude(-97.789805)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017, 4, 23, 0, 44, 25), ZoneId.systemDefault()))
        .sequence(1491703950L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.240856).longitude(-97.785817)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017, 4, 23, 0, 44, 49), ZoneId.systemDefault()))
        .sequence(1491703959L).valid(true).build()
    ));

    testedInstance.endRide(1L, new RideTracker());

    verify(rideTrackerDslRepository).saveAnyMany(savedTrackersCaptor.capture());
    List<RideTracker> savedTrackers = savedTrackersCaptor.getValue();
    assertTrue(savedTrackers.get(0).getValid());
    assertFalse(savedTrackers.get(1).getValid());
    assertTrue(savedTrackers.get(2).getValid());
  }

  @Test
  public void testRecalculateCachedUpdates(){
    when(rideTrackerDslRepository.findAllTrackerRecord(anyLong())).thenReturn(ImmutableList.of(
      RideTracker.builder()
        .latitude(30.266374).longitude(-97.7214293)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017, 5, 13, 10, 55, 19), ZoneId.systemDefault()))
        .sequence(0L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.266631).longitude(-97.721306)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017, 5, 13, 10, 55, 19), ZoneId.systemDefault()))
        .sequence(1494672404L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.266154).longitude(-97.720832)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017, 5, 13, 10, 55, 19), ZoneId.systemDefault()))
        .sequence(1494672407L).valid(true).build()
    ));

    testedInstance.endRide(1L, new RideTracker());

    verify(rideTrackerDslRepository).saveAnyMany(savedTrackersCaptor.capture());
    List<RideTracker> savedTrackers = savedTrackersCaptor.getValue();
    assertTrue(savedTrackers.get(0).getValid());
    assertTrue(savedTrackers.get(1).getValid());
    assertTrue(savedTrackers.get(2).getValid());
  }

  @Test
  public void testRecalculateWithTeleports_2() {
    when(rideTrackerDslRepository.findAllTrackerRecord(anyLong())).thenReturn(ImmutableList.of(
      RideTracker.builder()
        .latitude(30.240549).longitude(-97.785888)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,53,11), ZoneId.systemDefault()))
        .sequence(0L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.501611).longitude(-97.789805)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,53,15), ZoneId.systemDefault()))
        .sequence(1494831195L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.240856).longitude(-97.785817)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,53,21), ZoneId.systemDefault()))
        .sequence(1494831200L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.240858).longitude(-97.785785)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,53,27), ZoneId.systemDefault()))
        .sequence(1494831206L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.240858).longitude(-97.785785)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,53,33), ZoneId.systemDefault()))
        .sequence(1494831212L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.240836).longitude(-97.785792)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,53,38), ZoneId.systemDefault()))
        .sequence(1494831218L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.240862).longitude(-97.7858)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,53, 44), ZoneId.systemDefault()))
        .sequence(1494831224L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.240845).longitude(-97.785813)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,53, 50), ZoneId.systemDefault()))
        .sequence(1494831229L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.240902).longitude(-97.785809)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,53, 56), ZoneId.systemDefault()))
        .sequence(1494831235L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.240938).longitude(-97.78581)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,54, 2), ZoneId.systemDefault()))
        .sequence(1494831241L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.240908).longitude(-99.785792)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,54, 7), ZoneId.systemDefault()))
        .sequence(1494831247L).valid(true).build(),
      RideTracker.builder()
        .latitude(30.240931).longitude(-97.785827)
        .trackedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2017,5,15 ,6,54, 13), ZoneId.systemDefault()))
        .sequence(1494831253L).valid(true).build()
    ));

    testedInstance.endRide(1L, new RideTracker());

    verify(rideTrackerDslRepository).saveAnyMany(savedTrackersCaptor.capture());
    List<RideTracker> savedTrackers = savedTrackersCaptor.getValue();
    assertTrue(savedTrackers.get(0).getValid());
    assertFalse(savedTrackers.get(1).getValid());
    assertTrue(savedTrackers.get(2).getValid());
    assertTrue(savedTrackers.get(3).getValid());
    assertTrue(savedTrackers.get(4).getValid());
    assertTrue(savedTrackers.get(5).getValid());
    assertTrue(savedTrackers.get(6).getValid());
    assertTrue(savedTrackers.get(7).getValid());
    assertTrue(savedTrackers.get(8).getValid());
    assertTrue(savedTrackers.get(9).getValid());
    assertFalse(savedTrackers.get(10).getValid());
    assertTrue(savedTrackers.get(11).getValid());
  }

}