package com.rideaustin.applepay;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.joda.money.Money;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.service.CarTypeService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.model.States;
import com.rideaustin.test.setup.DefaultApplePaySetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(ApplePay.class)
public class C1181749IT extends AbstractApplePayTest<DefaultApplePaySetup> {

  @Inject
  private RidePaymentConfig rideServiceConfig;

  @Inject
  private CarTypeService carTypeService;

  @Test
  @TestCases("C1181749")
  public void test() throws Exception {
    LatLng pickupLocation = locationProvider.getCenter();
    ActiveDriver driver = setup.getActiveDriver();

    driverAction.goOnline(driver.getDriver().getEmail(), pickupLocation)
      .andExpect(status().isOk());

    Long ride = riderAction.requestRide(rider.getEmail(), pickupLocation, TestUtils.REGULAR, null, SAMPLE_TOKEN);

    awaitDispatch(driver, ride);

    driverAction.acceptRide(driver, ride)
      .andExpect(status().isOk());

    sleeper.sleep(rideServiceConfig.getCancellationChargeFreePeriod()*1000);

    riderAction.cancelRide(rider.getEmail(), ride)
      .andExpect(status().isOk());

    awaitState(ride, States.RIDER_CANCELLED, States.ENDED);
    awaitStatus(ride, RideStatus.RIDER_CANCELLED);

    paymentService.processRidePayment(ride);

    Money expectedCancellationFee = carTypeService.getCityCarType(TestUtils.REGULAR, 1L)
      .map(CityCarType::getCancellationFee).orElse(Constants.ZERO_USD);

    assertEquals(expectedCancellationFee, stripeServiceMock.getApplePayCharged());
  }

}
