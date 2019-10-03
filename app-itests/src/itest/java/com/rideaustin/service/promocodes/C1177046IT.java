package com.rideaustin.service.promocodes;

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
import com.rideaustin.test.setup.C1177046Setup;
import com.rideaustin.testrail.TestCases;

@Category(RiderPromocode.class)
public class C1177046IT extends AbstractNonTxPromocodeTest<C1177046Setup> {

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
  @TestCases("C1177046")
  public void shouldUseMultipleTimes() throws Exception {
    final Rider rider = setup.getRider();
    final ActiveDriver activeDriver = setup.getActiveDriver();
    final LatLng center = locationProvider.getCenter();

    final Long firstRideId = rideAction.performRide(rider, center, center, activeDriver);

    final Ride firstRide = rideDslRepository.findOne(firstRideId);

    PromocodeRedemption redemption = redemptionDslRepository.findOne(setup.getRedemption().getId());

    assertThat(redemption.isActive()).isTrue();
    assertUsed(firstRide, redemption, 1);

    // Second ride
    final Long secondRideId = rideAction.performRide(rider, center, center, activeDriver);

    final Ride secondRide = rideDslRepository.findOne(secondRideId);

    redemption = redemptionDslRepository.findOne(setup.getRedemption().getId());

    assertThat(redemption.isActive()).isTrue();
    assertUsed(secondRide, redemption, 2, firstRide.getFreeCreditCharged().getAmount());

    // Third ride
    final Long thirdRideId = rideAction.performRide(rider, center, center, activeDriver);

    final Ride thirdRide = rideDslRepository.findOne(thirdRideId);

    redemption = redemptionDslRepository.findOne(setup.getRedemption().getId());

    assertThat(redemption.isActive()).isTrue();
    assertUsed(thirdRide, redemption, 3, firstRide.getFreeCreditCharged().getAmount(), thirdRide.getFreeCreditCharged().getAmount());
  }
}
