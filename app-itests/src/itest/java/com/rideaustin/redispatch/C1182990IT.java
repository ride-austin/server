package com.rideaustin.redispatch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.test.asserts.DispatchHistoryAssert;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182990
 */
@Category(Redispatch.class)
public class C1182990IT extends Abstract4DriversRedispatchTest {

  @Test
  @TestCases("C1182990")
  public void test() throws Exception {
    doTestRedispatch(null);
  }

  @Override
  protected void acceptRide(ActiveDriver driver, Long ride) throws Exception {
    awaitDispatch(driver, ride, dispatchTimeout, environment, contextAccess);
    logger.info("Ride is assigned to first driver");
    driverAction.declineRide(driver, ride)
      .andExpect(status().isOk());
    logger.info("Ride is declined by first driver");
  }

  @Override
  protected void acceptRedispatched(ActiveDriver driver, Long ride) throws Exception {
    super.acceptRide(driver, ride);
  }

  @Override
  protected MobileRiderRideDto assertRideAssigned(ActiveDriver driver, LatLng riderLocation, LatLng driverLocation, Long ride) throws Exception {
    return riderAction.getRideInfo(rider.getEmail(), ride);
  }

  @Override
  protected void cancelAndRedispatch(ActiveDriver firstDriver, ActiveDriver secondDriver, Long ride) throws Exception {
    logger.info("Entering cancel and redispatch");
    logger.info("Waiting for ride to be assigned to second driver");

    logger.info("Ride is assigned to second driver");
    driverAction.acceptRide(secondDriver, ride)
      .andExpect(status().isOk());
    logger.info("Ride is accepted by second driver");
    driverAction.cancelRide(secondDriver.getDriver().getEmail(), ride);
    logger.info("Ride is cancelled by second driver");

    logger.info("Waiting for ride to be assigned to third driver");

    awaitDispatch(thirdDriver, ride, dispatchTimeout, environment, contextAccess);
    logger.info("Ride is assigned to third driver");
    driverAction.declineRide(thirdDriver, ride)
      .andExpect(status().isOk());
    logger.info("Ride is declined by third driver");

    acceptRedispatched(fourthDriver, ride);
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    super.assertRideRedispatched(destination, secondDriver, riderLocation, secondDriverLocation, ride);

    DispatchHistoryAssert.assertThat(getDispatchHistory(ride))
      .isDispatchedFirstTo(firstDriver.getId())
      .thenIsDispatchedTo(secondDriver.getId())
      .thenIsDispatchedTo(thirdDriver.getId())
      .thenIsDispatchedTo(fourthDriver.getId());
  }
}
