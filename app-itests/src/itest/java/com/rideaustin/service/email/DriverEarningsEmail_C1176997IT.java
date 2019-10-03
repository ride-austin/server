package com.rideaustin.service.email;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.Constants;
import com.rideaustin.filter.ClientType;
import com.rideaustin.jobs.EarningsEmailJob;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.fixtures.RideFixture;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.testrail.TestCases;

public class DriverEarningsEmail_C1176997IT extends AbstractEarningsEmailTest {

  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "rideAcceptance", "allowancePeriod", 5);
  }

  @Test
  @TestCases("C1176997")
  public void shouldSendEarningsEmail_WhenJobIsTriggered() throws Exception {
    final double surgeFactor = 2.0;
    Ride ride = newRide(surgeFactor);
    Driver driver = ride.getActiveDriver().getDriver();

    driverAction.endRide(driver.getEmail(), ride.getId(),
      ride.getStartLocationLat(), ride.getStartLocationLong())
      .andExpect(status().isOk());
    riderAction.rateRide(ride.getRider().getEmail(), ride.getId(), new BigDecimal(5))
      .andExpect(status().isOk());

    triggerJob(driver, EarningsEmailJob.class);

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).earningsEmailDelivered(startDate, driver.getEmail());
  }

  private Ride newRide(double surgeFactor) {
    RideFixture rideFixture = rideFixtureProvider.create(RideStatus.ACTIVE, false, builder ->
      builder.surgeFactor(surgeFactor));
    return rideFixture.getFixture();
  }

}
