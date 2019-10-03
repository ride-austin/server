package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Test;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.setup.RA12423Setup;
import com.rideaustin.test.util.TestUtils;

public class RA12423IT extends AbstractSplitFarePromocodeApplicableToFeesTest<RA12423Setup> {

  @Test
  public void test() throws Exception {
    LatLng center = locationProvider.getCenter();
    riderAction.usePromocode(rider, promocode)
      .andExpect(status().isOk());
    driverAction.locationUpdate(activeDriver, center.lat, center.lng, new String[]{TestUtils.PREMIUM})
      .andExpect(status().isOk());
    Long ride = riderAction.requestRide(rider.getEmail(), center, TestUtils.PREMIUM);

    awaitDispatch(activeDriver, ride);

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction)
      .withRideId(ride);

    cascadedDriverAction.acceptRide()
      .reach()
      .startRide()
      .endRide(center.lat, center.lng);

    awaitStatus(ride, RideStatus.COMPLETED);

    paymentService.processRidePayment(ride);

    List<FarePayment> farePayments = farePaymentService.getAcceptedPaymentParticipants(ride);
    assertEquals(1, farePayments.size());
    assertEquals(Constants.ZERO_USD, farePayments.get(0).getStripeCreditCharge());
  }
}
