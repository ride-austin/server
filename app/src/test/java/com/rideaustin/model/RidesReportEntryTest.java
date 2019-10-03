package com.rideaustin.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;

import org.junit.Test;

import com.rideaustin.service.model.RideReportEntry;

public class RidesReportEntryTest {
  @Test
  public void testMilesConvert() {
    RideReportEntry dre = new RideReportEntry(null, null, new BigDecimal(10000), new BigDecimal(20000), null, null, null);
    assertThat(dre.getAverageDistanceTraveled().doubleValue(), is(20000d));
    assertThat(dre.getDistanceTraveled().doubleValue(), is(10000d));
    assertThat(dre.getAverageDistanceTraveledInMiles().doubleValue(), is(12.43));
    assertThat(dre.getDistanceTraveledInMiles().doubleValue(), is(6.21));
  }
}
