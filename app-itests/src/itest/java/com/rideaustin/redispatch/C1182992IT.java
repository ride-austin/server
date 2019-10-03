package com.rideaustin.redispatch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.payment.PaymentService;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182992
 */
@Category(Redispatch.class)
public class C1182992IT extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  @Inject
  private PaymentService paymentService;

  private LatLng destination;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    destination = locationProvider.getAirportLocation();
  }

  @Test
  @TestCases("C1182992")
  public void test() throws Exception {
    doTestRedispatch(destination);
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(secondDriver.getDriver(), driverAction)
      .withRideId(ride);
    cascadedDriverAction
      .reach()
      .startRide()
      .endRide(destination.lat, destination.lng);

    riderAction.rateRide(rider.getEmail(), ride, BigDecimal.valueOf(5.0), BigDecimal.valueOf(5.0), null)
      .andExpect(status().isOk());

    paymentService.processRidePayment(ride);

    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);
    RiderRideAssert.assertThat(rideInfo)
      .hasRequestedCategory(TestUtils.REGULAR)
      .hasDriverAssigned(secondDriver.getId())
      .hasStartLocation(riderLocation)
      .hasEndLocation(destination);
  }
}
