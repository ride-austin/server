package com.rideaustin.service.model;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.Constants;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;

public class DriverEarningsTest {

  private DriverEarnings testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new DriverEarnings(LocalDate.now(), new Driver(), Constants.CST_ZONE);
  }

  @Test
  public void testGetHoursOnlineFormatsSecondsAsTime() throws Exception {
    ActiveDriver ad = new ActiveDriver();
    ad.setCreatedDate(Date.from(LocalDateTime.of(2016, 10, 4, 12, 5, 5).toInstant(ZoneOffset.UTC)));
    ad.setUpdatedDate(Date.from(LocalDateTime.of(2016, 10, 4, 13, 7, 17).toInstant(ZoneOffset.UTC)));
    testedInstance.addActiveDriver(ad,
      Date.from(LocalDateTime.of(2016, 10, 4, 12, 5, 6).toInstant(ZoneOffset.UTC)),
      Date.from(LocalDateTime.of(2016, 10, 4, 13, 7, 16).toInstant(ZoneOffset.UTC)));

    String actual = testedInstance.getHoursOnline();

    assertEquals("01:02:10", actual);
  }
}