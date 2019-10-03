package com.rideaustin.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.math.BigDecimal;

import org.junit.Test;

import com.rideaustin.service.model.DriverRidesReportEntry;

public class DriverRidesReportEntryTest {
  @Test
  public void testMilesConvert() {
    DriverRidesReportEntry dre = new DriverRidesReportEntry(null, null, null, null,
      null, null, new BigDecimal(10000), null, null, null, null, null, null, null
    );
    assertThat(dre.getDistanceTravelled().doubleValue(), is(10000d));
    assertThat(dre.getDistanceTraveledInMiles().doubleValue(), is(6.21));
  }

  @Test
  public void testMilesConvertNulls() {
    DriverRidesReportEntry dre = new DriverRidesReportEntry();
    assertThat(dre.getDistanceTravelled(), is(nullValue()));
    assertThat(dre.getDistanceTraveledInMiles(), is(nullValue()));
  }
}