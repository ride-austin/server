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
public class RiderRating_C1177109IT extends AbstractRatingTest {

  @Test
  @TestCases("C1177009")
  public void shouldCalculateRatingUpgrades_WithLimit() throws Exception {
    Ride ride = null;
    final BigDecimal firstRating = new BigDecimal(1.0);
    final int firstRatingRound = 10;
    for (int i = 0; i < firstRatingRound; i++) {
      ride = newRide(COMPLETED);
      driverAction.rateRide(ride.getActiveDriver().getDriver().getEmail(), ride.getId(), firstRating)
        .andExpect(status().isOk());
    }

    final BigDecimal secondRating = new BigDecimal(4.0);
    final int secondRatingRound = riderRatingService.getLimit();
    for (int i = 0; i < secondRatingRound; i++) {
      ride = newRide(COMPLETED);
      driverAction.rateRide(ride.getActiveDriver().getDriver().getEmail(), ride.getId(), secondRating)
        .andExpect(status().isOk());
    }

    double actualRating = ride.getRider().getRating();
    assertThat(actualRating).isEqualTo(secondRating.doubleValue());
  }
}

