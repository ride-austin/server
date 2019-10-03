package com.rideaustin.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.service.DefaultSearchDriverHandler.FingerprintPriorityComparator;
import com.rideaustin.service.model.ConsecutiveDeclinedRequestsData;
import com.rideaustin.service.model.OnlineDriverDto;

public class FingerprintPriorityComparatorTest {

  private final static Integer FINGERPRINTED_BITMASK = 4;

  private FingerprintPriorityComparator testedInstance = new FingerprintPriorityComparator(FINGERPRINTED_BITMASK, 120);
  private OnlineDriverDto fpDriver;
  private OnlineDriverDto fpDriver1;
  private OnlineDriverDto noFPDriver;
  private OnlineDriverDto noFPDriver1;

  @Before
  public void setUp() throws Exception {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    fpDriver = new OnlineDriverDto(random.nextLong(), ActiveDriverStatus.AVAILABLE, random.nextLong(), random.nextLong(),
      1, 4, 1L, "", "", new ConsecutiveDeclinedRequestsData(), "");
    fpDriver1 = new OnlineDriverDto(random.nextLong(), ActiveDriverStatus.AVAILABLE, random.nextLong(), random.nextLong(),
      1, 4, 1L, "", "", new ConsecutiveDeclinedRequestsData(), "");
    noFPDriver = new OnlineDriverDto(random.nextLong(), ActiveDriverStatus.AVAILABLE, random.nextLong(), random.nextLong(),
      1, 0, 1L, "", "", new ConsecutiveDeclinedRequestsData(), "");
    noFPDriver1 = new OnlineDriverDto(random.nextLong(), ActiveDriverStatus.AVAILABLE, random.nextLong(), random.nextLong(),
      1, 0, 1L, "", "", new ConsecutiveDeclinedRequestsData(), "");
  }

  @Test
  public void testCompare_WhenFPIsFurther_AndWithinThreshold() {
    fpDriver.setDrivingTimeToRider(100L);
    noFPDriver.setDrivingTimeToRider(50L);
    final List<OnlineDriverDto> drivers = new ArrayList<>(ImmutableList.of(noFPDriver, fpDriver));
    drivers.sort(Comparator.comparing(OnlineDriverDto::getDrivingTimeToRider));

    drivers.sort(testedInstance);

    assertEquals(FINGERPRINTED_BITMASK, drivers.get(0).getAvailableDriverTypesBitmask());
  }

  @Test
  public void testCompare_WhenFPIsFurther_AndNotWithinThreshold() {
    fpDriver.setDrivingTimeToRider(500L);
    noFPDriver.setDrivingTimeToRider(50L);
    final List<OnlineDriverDto> drivers = new ArrayList<>(ImmutableList.of(noFPDriver, fpDriver));
    drivers.sort(Comparator.comparing(OnlineDriverDto::getDrivingTimeToRider));

    drivers.sort(testedInstance);

    assertEquals(FINGERPRINTED_BITMASK, drivers.get(1).getAvailableDriverTypesBitmask());
  }

  @Test
  public void testCompare_WhenFPIsNearer() {
    fpDriver.setDrivingTimeToRider(20L);
    noFPDriver.setDrivingTimeToRider(50L);
    final List<OnlineDriverDto> drivers = new ArrayList<>(ImmutableList.of(noFPDriver, fpDriver));
    drivers.sort(Comparator.comparing(OnlineDriverDto::getDrivingTimeToRider));

    drivers.sort(testedInstance);

    assertEquals(FINGERPRINTED_BITMASK, drivers.get(0).getAvailableDriverTypesBitmask());
  }

  @Test
  public void testCompare_WhenBothAreFP() {
    testWhenBothHaveSameDriverType(fpDriver, fpDriver1);
  }

  @Test
  public void testCompare_WhenBothAreNoFP() {
    testWhenBothHaveSameDriverType(noFPDriver, noFPDriver1);
  }

  protected void testWhenBothHaveSameDriverType(OnlineDriverDto driver1, OnlineDriverDto driver2) {
    driver1.setDrivingTimeToRider(100L);
    driver2.setDrivingTimeToRider(50L);
    final List<OnlineDriverDto> drivers = new ArrayList<>(ImmutableList.of(driver2, driver1));
    drivers.sort(Comparator.comparing(OnlineDriverDto::getDrivingTimeToRider));

    drivers.sort(testedInstance);

    assertEquals(driver2.getId(), drivers.get(0).getId());
    assertEquals(driver1.getId(), drivers.get(1).getId());
  }
}
