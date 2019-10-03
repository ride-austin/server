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
public class C1177039DriverIT extends BaseC1177039Test {

  @Test
  @TestCases("C1177039")
  public void shouldNotReducePromocode_WhenDriverCancelsLate() throws Exception {
    Long ride = setupRide();
    driverAction.acceptRide(activeDriver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());
    driverAction.reach(activeDriver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());
    sleeper.sleep(30000);

    driverAction.cancelRide(activeDriver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());
    awaitStatus(ride, RideStatus.DRIVER_CANCELLED);

    Ride foundRide = rideDslRepository.findOne(ride);
    assertThat(foundRide.getCancellationFee().getAmount()).isEqualByComparingTo(cancellationFee);
    assertNotUsed(foundRide, redemption, 0);
  }

}
