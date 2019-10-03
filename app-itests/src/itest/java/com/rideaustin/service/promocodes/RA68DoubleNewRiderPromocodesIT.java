package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.google.maps.model.LatLng;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.PromocodeRedemptionDslRepository;
import com.rideaustin.test.actions.RideAction;
import com.rideaustin.test.setup.RA68DoubleNewRiderSetup;
import com.rideaustin.test.stubs.transactional.PaymentService;

public class RA68DoubleNewRiderPromocodesIT extends AbstractNonTxPromocodeTest<RA68DoubleNewRiderSetup> {

  @Inject
  private RideAction rideAction;

  @Inject
  private PromocodeRedemptionDslRepository redemptionDslRepository;

  @Inject
  private PaymentService paymentService;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  public void test() throws Exception {
    final Rider rider = setup.getRider();
    final ActiveDriver activeDriver = setup.getActiveDriver();
    final LatLng center = locationProvider.getCenter();

    final Long rideId = rideAction.performRide(rider, center, center, activeDriver);

    final Ride ride = rideDslRepository.findOne(rideId);

    assertNotNull(ride.getPromocodeRedemptionId());

    paymentService.processRidePayment(ride);

    PromocodeRedemption unusedRedemption = null;
    if (ride.getPromocodeRedemptionId().equals(setup.getFirstRedemption().getId())) {
      unusedRedemption = redemptionDslRepository.findOne(setup.getSecondRedemption().getId());
    } else if (ride.getPromocodeRedemptionId().equals(setup.getSecondRedemption().getId())) {
      unusedRedemption = redemptionDslRepository.findOne(setup.getFirstRedemption().getId());
    } else {
      fail("None of redemptions were used");
    }

    assertFalse(unusedRedemption.isActive());

  }
}
