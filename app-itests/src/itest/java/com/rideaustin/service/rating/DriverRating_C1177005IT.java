package com.rideaustin.service.rating;

import static com.rideaustin.model.enums.RideStatus.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.Constants;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.testrail.TestCases;

@Category(Rating.class)
public class DriverRating_C1177005IT extends AbstractRatingTest {

  @Inject
  private RidePaymentConfig rideServiceConfig;

  private static final boolean SAME_DRIVER = true;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "upfrontEnabled", false);
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "asyncPreauthEnabled", false);
  }

  @Test
  @TestCases("C1177005")
  public void shouldNotAffectDriverRating_WhenDriverCancelsRide() throws Exception {
    final int rideCount = driverRatingService.getMinimumRatingThreshold();
    final BigDecimal rating = new BigDecimal(1.0);

    Ride ride = null;
    for (int i = 0; i < rideCount; i++) {
      ride = newRide(COMPLETED, SAME_DRIVER);
      riderAction.rateRide(ride.getRider().getEmail(), ride.getId(), rating)
        .andExpect(status().isOk());
    }

    assertThat(ride.getActiveDriver().getDriver().getRating()).isEqualTo(rating.doubleValue());

    Ride toBeCancelled = getRideThatDriverReachedAlready();
    driverAction.cancelRide(ride.getActiveDriver().getDriver().getEmail(), toBeCancelled.getId())
      .andExpect(status().isOk());

    assertThat(ride.getActiveDriver().getDriver().getRating()).isEqualTo(rating.doubleValue());
  }

  private Ride getRideThatDriverReachedAlready() {
    Ride ride = newRide(RideStatus.DRIVER_REACHED, SAME_DRIVER);
    Instant updated = Instant.now().minus(rideServiceConfig.getCancellationChargeFreePeriod(), ChronoUnit.SECONDS);
    ride.setDriverReachedOn(Date.from(updated));
    return ride;
  }
}

