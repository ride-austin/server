package com.rideaustin.payment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.Constants;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.setup.UpfrontPricingSetup;

public class AsyncPreauthNADIT extends AbstractUpfrontPricingTest<UpfrontPricingSetup> {

  @Inject
  private RiderAction riderAction;
  @Inject
  private DriverAction driverAction;

  private Rider rider;
  private ActiveDriver activeDriver;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    rider = setup.getRider();
    activeDriver = setup.getActiveDriver();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "asyncPreauthEnabled", true);
  }

  @Test
  public void testNoDriver() throws Exception {
    stripeServiceMock.setPreauthTimeout(500L);
    final Long rideId = riderAction.requestRide(rider.getEmail(), locationProvider.getCenter());

    awaitStatus(rideId, RideStatus.NO_AVAILABLE_DRIVER);

    sleeper.sleep(2000);

    assertFalse(stripeServiceMock.isPreauthCompleted());
    assertFalse(stripeServiceMock.isPreauthRefunded());
  }

  @Test
  public void testRiderCancelImmediately() throws Exception {
    stripeServiceMock.setPreauthTimeout(1000L);

    driverAction.goOnline(activeDriver.getDriver().getEmail(), locationProvider.getCenter());
    final Long rideId = riderAction.requestRide(rider.getEmail(), locationProvider.getCenter());

    sleeper.sleep(200);

    riderAction.cancelRide(rider.getEmail(), rideId);

    awaitStatus(rideId, RideStatus.RIDER_CANCELLED);

    sleeper.sleep(2000);

    assertTrue(stripeServiceMock.isPreauthCompleted());
    assertTrue(stripeServiceMock.isPreauthRefunded());
  }

}
