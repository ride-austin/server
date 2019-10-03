package com.rideaustin.service.promocodes;

import static com.rideaustin.service.promocodes.AbstractPromocodeTest.assertNotUsed;
import static com.rideaustin.service.promocodes.AbstractPromocodeTest.assertUsed;
import static org.assertj.core.api.Assertions.assertThat;

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
import com.rideaustin.test.setup.C1177047Setup;
import com.rideaustin.testrail.TestCases;

@Category(RiderPromocode.class)
public class C1177047IT extends AbstractNonTxPromocodeTest<C1177047Setup> {

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
  @TestCases("C1177047")
  public void shouldNotUseMultipleTimes() throws Exception {
    final Rider rider = setup.getRider();
    final ActiveDriver activeDriver = setup.getActiveDriver();
    final LatLng center = locationProvider.getCenter();

    final Long firstRideId = rideAction.performRide(rider, center, center, activeDriver);

    final Ride firstRide = rideDslRepository.findOne(firstRideId);

    PromocodeRedemption redemption = redemptionDslRepository.findOne(setup.getRedemption().getId());

    assertThat(redemption.isActive()).isFalse();
    assertUsed(firstRide, redemption, 1);

    // Second ride
    final Long secondRideId = rideAction.performRide(rider, center, center, activeDriver);

    final Ride secondRide = rideDslRepository.findOne(secondRideId);

    redemption = redemptionDslRepository.findOne(setup.getRedemption().getId());

    assertThat(redemption.isActive()).isFalse();
    assertNotUsed(secondRide, redemption, 1, firstRide.getFreeCreditCharged().getAmount());

    // Third ride
    final Long thirdRideId = rideAction.performRide(rider, center, center, activeDriver);

    final Ride thirdRide = rideDslRepository.findOne(thirdRideId);

    redemption = redemptionDslRepository.findOne(setup.getRedemption().getId());

    assertThat(redemption.isActive()).isFalse();
    assertNotUsed(thirdRide, redemption, 1, firstRide.getFreeCreditCharged().getAmount());
  }
}
