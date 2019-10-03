package com.rideaustin.service.rating;

import static com.rideaustin.model.enums.RideStatus.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.Constants;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.testrail.TestCases;

@Category(Rating.class)
public class DriverRating_C1177002IT extends AbstractRatingTest {

  private static final boolean SAME_DRIVER = true;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "asyncPreauthEnabled", false);
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "upfrontEnabled", false);
  }

  @Test
  @TestCases("C1177002")
  public void shouldBeFive_WhenMinimumThresholdIsNotPassed() throws Exception {
    final double expectedRating = riderRatingService.getDefaultRating();
    final BigDecimal rating = new BigDecimal(1.0);
    Ride ride = newRide(COMPLETED, SAME_DRIVER);

    riderAction.rateRide(ride.getRider().getEmail(), ride.getId(), rating)
      .andExpect(status().isOk());

    double actualRating = ride.getActiveDriver().getDriver().getRating();
    assertThat(actualRating).isEqualTo(expectedRating);
  }

  @Test
  @TestCases("C1177002")
  public void shouldCalculateAverage_WhenEqualToMinimumThreshold() throws Exception {
    final int rideCount = driverRatingService.getMinimumRatingThreshold();
    final BigDecimal rating = new BigDecimal(1.0);

    Ride ride = null;
    for (int i = 0; i < rideCount; i++) {
      ride = newRide(COMPLETED, SAME_DRIVER);
      riderAction.rateRide(ride.getRider().getEmail(), ride.getId(), rating)
        .andExpect(status().isOk());
    }

    double actualRating = ride.getActiveDriver().getDriver().getRating();
    assertThat(actualRating).isEqualTo(rating.doubleValue());
  }

  @Test
  @TestCases("C1177002")
  public void shouldCalculateAverage_WhenMinimumThresholdIsPassed() throws Exception {
    final int rideCount = 10;
    final BigDecimal[] ratings = randomRatings(rideCount);

    Ride ride = null;
    for (int i = 0; i < rideCount; i++) {
      ride = newRide(COMPLETED, SAME_DRIVER);
      riderAction.rateRide(ride.getRider().getEmail(), ride.getId(), ratings[i])
        .andExpect(status().isOk());
    }

    double actualRating = ride.getActiveDriver().getDriver().getRating();
    assertThat(actualRating).isEqualTo(average(ratings));
  }
}

