package com.rideaustin.redispatch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.setup.C1182975Setup;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182975
 */
@Category({Redispatch.class})
public class C1182975IT extends AbstractSurgeRedispatchTest<C1182975Setup> {

  @Test
  @TestCases("C1182975")
  public void test() throws Exception {
    doTestRedispatch(null);
  }

  @Override
  protected Long requestAndAccept(LatLng destination, ActiveDriver firstDriver, LatLng riderLocation) throws Exception {
    Long ride = riderAction.requestRide(rider.getEmail(), riderLocation, destination, true);

    awaitDispatch(firstDriver, ride, dispatchTimeout, environment, contextAccess);

    driverAction.acceptRide(firstDriver, ride)
      .andExpect(status().isOk());
    schedulerService.executeNext();
    administratorAction.updateSurgeFactor(administrator.getEmail(), surgeArea, getNewFactor());
    return ride;
  }

  @Override
  protected BigDecimal getNewFactor() {
    return BigDecimal.ONE;
  }

  @Override
  protected BigDecimal getExpectedSurgeFare() {
    return BigDecimal.valueOf(5.0);
  }

}
