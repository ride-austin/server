package com.rideaustin.service.promocodes;

import static com.rideaustin.Constants.City.AUSTIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.Constants;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.testrail.TestCases;

@Category(RiderPromocode.class)
public class C1177037IT extends AbstractPromocodeTest {

  private Double promoCredit;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Before
  public void setUp() {
    promoCredit = getMinimumFareForCityCarType(carTypeRegularFixture, AUSTIN.getId()).doubleValue();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "asyncPreauthEnabled", false);
  }

  @Test
  @TestCases("C1177037")
  public void shouldNotApplyPromocode_WhenRiderCancels_WithoutFee() throws Exception {
    RiderFixture riderFixture = riderFixtureProvider.create();
    Ride ride = newRide(RideStatus.DRIVER_ASSIGNED, riderFixture);
    redemption = newRedemption(riderFixture, promoCredit);

    riderAction.cancelRide(ride.getRider().getEmail(), ride.getId())
      .andExpect(status().isOk());

    assertThat(ride.getCancellationFee().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertNotUsed(ride, redemption, 0);
  }

  @Test
  @TestCases("C1177037")
  public void shouldNotApplyPromocode_WhenDriverCancels_WithoutFee() throws Exception {
    RiderFixture riderFixture = riderFixtureProvider.create();
    Ride ride = newRide(RideStatus.DRIVER_REACHED, riderFixture);
    redemption = newRedemption(riderFixture, promoCredit);

    driverAction.cancelRide(ride.getActiveDriver().getDriver().getEmail(), ride.getId())
      .andExpect(status().isOk());

    assertThat(ride.getCancellationFee().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertNotUsed(ride, redemption, 0);
  }
}
