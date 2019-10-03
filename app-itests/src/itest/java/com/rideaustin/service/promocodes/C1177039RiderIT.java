package com.rideaustin.service.promocodes;

import static com.rideaustin.service.promocodes.AbstractPromocodeTest.assertNotUsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.testrail.TestCases;

@Category(RiderPromocode.class)
public class C1177039RiderIT extends BaseC1177039Test {

  @Test
  @TestCases("C1177039")
  public void shouldNotReducePromocode_WhenRiderCancelsLate() throws Exception {
    Long ride = setupRide();
    driverAction.acceptRide(activeDriver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());
    sleeper.sleep(30000);

    riderAction.cancelRide(rider.getEmail(), ride)
      .andExpect(status().isOk());
    awaitStatus(ride, RideStatus.RIDER_CANCELLED);

    Ride foundRide = rideDslRepository.findOne(ride);
    assertThat(foundRide.getCancellationFee().getAmount()).isEqualByComparingTo(cancellationFee);
    assertNotUsed(foundRide, redemption, 0);
  }
}
