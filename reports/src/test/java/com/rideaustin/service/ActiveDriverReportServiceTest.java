package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;

public class ActiveDriverReportServiceTest {

  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;

  private ActiveDriverReportService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ActiveDriverReportService(activeDriverDslRepository);
  }

  @Test
  public void getDriverOnlineSecondsSumsActiveTime() {
    final ActiveDriver activeDriver = new ActiveDriver();
    final Date date = new Date();
    final Date createdDate = DateUtils.addSeconds(date, -5);
    final Date inactiveOn = DateUtils.addSeconds(date, 5);
    activeDriver.setCreatedDate(createdDate);
    activeDriver.setInactiveOn(inactiveOn);
    when(activeDriverDslRepository.getActiveDrivers(any(Driver.class), any(Date.class), any(Date.class)))
      .thenReturn(Arrays.asList(activeDriver));

    final long result = testedInstance.getDriverOnlineSeconds(new Driver(), createdDate.toInstant(), inactiveOn.toInstant());

    assertEquals(10L, result);
  }
}