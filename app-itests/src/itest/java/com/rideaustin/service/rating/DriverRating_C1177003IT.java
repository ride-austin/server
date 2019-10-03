package com.rideaustin.service.rating;

import static com.rideaustin.model.enums.RideStatus.COMPLETED;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.Constants;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.testrail.TestCases;

@Category(Rating.class)
public class DriverRating_C1177003IT extends AbstractRatingTest {

  private static final boolean SAME_DRIVER = true;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "redispatchOnCancel", "enabled", false);
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "upfrontEnabled", false);
  }

  @Test
  @TestCases("C1177003")
  public void shouldCalculateRatingUpgrades_WithLimit() throws Exception {
    Ride ride = null;
    final BigDecimal firstRating = new BigDecimal(1.0);
    final int firstRatingRound = 10;
    for (int i = 0; i < firstRatingRound; i++) {
      ride = newRide(COMPLETED, SAME_DRIVER);
      riderAction.rateRide(ride.getRider().getEmail(), ride.getId(), firstRating)
        .andExpect(status().isOk());
    }

    final BigDecimal secondRating = new BigDecimal(4.0);
    final int secondRatingRound = riderRatingService.getLimit();
    for (int i = 0; i < secondRatingRound; i++) {
      ride = newRide(COMPLETED, SAME_DRIVER);
      riderAction.rateRide(ride.getRider().getEmail(), ride.getId(), secondRating)
        .andExpect(status().isOk());
    }

    double actualRating = ride.getActiveDriver().getDriver().getRating();
    Assertions.assertThat(actualRating).isEqualTo(secondRating.doubleValue());
  }
}

