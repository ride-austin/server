package com.rideaustin.payment;

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

public class AsyncPreauthNAD_DriverDeclineIT extends AbstractUpfrontPricingTest<UpfrontPricingSetup> {

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
  public void testDriverDecline() throws Exception {
    stripeServiceMock.setPreauthTimeout(1000L);

    driverAction.goOnline(activeDriver.getDriver().getEmail(), locationProvider.getCenter());
    final Long rideId = riderAction.requestRide(rider.getEmail(), locationProvider.getCenter());

    sleeper.sleep(2000);

    driverAction.declineRide(activeDriver, rideId);

    awaitStatus(rideId, RideStatus.NO_AVAILABLE_DRIVER);

    assertTrue(stripeServiceMock.isPreauthCompleted());
    assertTrue(stripeServiceMock.isPreauthRefunded());
  }

}
