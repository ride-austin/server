package com.rideaustin.service.promocodes;

import static com.rideaustin.service.promocodes.AbstractPromocodeTest.assertNotUsed;
import static com.rideaustin.service.promocodes.AbstractPromocodeTest.assertUsed;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.PromocodeRedemptionDslRepository;
import com.rideaustin.test.actions.RideAction;
import com.rideaustin.test.setup.C1177052Setup;
import com.rideaustin.testrail.TestCases;

@Category(RiderPromocode.class)
public class C1177052IT extends AbstractNonTxPromocodeTest<C1177052Setup> {

  @Inject
  private RideAction rideAction;
  @Inject
  private PromocodeRedemptionDslRepository redemptionDslRepository;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  @TestCases("C1177052")
  public void shouldApplyMultiUsePromocode() throws Exception {
    final Rider rider = setup.getRider();
    final ActiveDriver activeDriver = setup.getActiveDriver();
    final LatLng center = locationProvider.getCenter();

    final Long firstRideId = rideAction.performRide(rider, center, center, activeDriver);

    final Ride firstRide = rideDslRepository.findOne(firstRideId);

    PromocodeRedemption firstRedemption = redemptionDslRepository.findOne(setup.getRedemption().getId());
    PromocodeRedemption secondRedemption = redemptionDslRepository.findOne(setup.getSecondRedemption().getId());

    assertUsed(firstRide, firstRedemption, 1);
    BigDecimal chargedFromOtherPromo = firstRide.getFreeCreditCharged().getAmount();
    assertNotUsed(firstRide, chargedFromOtherPromo, secondRedemption, 0);

    // Second Ride
    final Long secondRideId = rideAction.performRide(rider, center, center, activeDriver);

    final Ride secondRide = rideDslRepository.findOne(secondRideId);

    firstRedemption = redemptionDslRepository.findOne(setup.getRedemption().getId());
    secondRedemption = redemptionDslRepository.findOne(setup.getSecondRedemption().getId());

    assertUsed(secondRide, firstRedemption, 2, secondRide.getFreeCreditCharged().getAmount());
    chargedFromOtherPromo = secondRide.getFreeCreditCharged().getAmount();
    assertNotUsed(secondRide, chargedFromOtherPromo, secondRedemption, 0);
  }
}
