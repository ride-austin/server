package com.rideaustin.service.rating;

import static com.rideaustin.model.enums.RideStatus.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.testrail.TestCases;

@Category(Rating.class)
public class RiderRating_C1177108IT extends AbstractRatingTest {

  @Test
  @TestCases("C1177008")
  public void shouldBeFive_WhenMinimumThresholdIsNotPassed() throws Exception {
    final double expectedRating = riderRatingService.getDefaultRating();
    final BigDecimal rating = new BigDecimal(1.0);
    Ride ride = newRide(COMPLETED);

    driverAction.rateRide(ride.getActiveDriver().getDriver().getEmail(), ride.getId(), rating)
      .andExpect(status().isOk());

    assertThat(ride.getRider().getRating()).isEqualTo(expectedRating);
  }

  @Test
  @TestCases("C1177008")
  public void shouldCalculateAverage_WhenEqualToMinimumThreshold() throws Exception {
    final int rideCount = driverRatingService.getMinimumRatingThreshold();
    final BigDecimal rating = new BigDecimal(1.0);

    Ride ride = null;
    for (int i = 0; i < rideCount; i++) {
      ride = newRide(COMPLETED);
      driverAction.rateRide(ride.getActiveDriver().getDriver().getEmail(), ride.getId(), rating)
        .andExpect(status().isOk());
    }

    assertThat(ride.getRider().getRating()).isEqualTo(rating.doubleValue());
  }

  @Test
  @TestCases("C1177008")
  public void shouldCalculateAverage_WhenMinimumThresholdIsPassed() throws Exception {
    final int rideCount = 10;
    final BigDecimal[] ratings = randomRatings(rideCount);

    Ride ride = null;
    for (int i = 0; i < rideCount; i++) {
      ride = newRide(COMPLETED);
      driverAction.rateRide(ride.getActiveDriver().getDriver().getEmail(), ride.getId(), ratings[i])
        .andExpect(status().isOk());
    }

    double actualRating = ride.getRider().getRating();
    assertThat(actualRating).isEqualTo(average(ratings));
  }
}

