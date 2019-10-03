package com.rideaustin.service.promocodes;

import static com.rideaustin.service.promocodes.AbstractPromocodeTest.assertUsed;

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
import com.rideaustin.test.setup.C1177039Setup;
import com.rideaustin.testrail.TestCases;

@Category(RiderPromocode.class)
public class C1177040IT extends AbstractNonTxPromocodeTest<C1177039Setup> {

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
  @TestCases("C1177040")
  public void shouldApplyPromocode_ForAustin() throws Exception {
    final Rider rider = setup.getRider();
    final ActiveDriver activeDriver = setup.getActiveDriver();
    final LatLng center = locationProvider.getCenter();

    final Long rideId = rideAction.performRide(rider, center, center, activeDriver);

    final Ride ride = rideDslRepository.findOne(rideId);
    final PromocodeRedemption redemption = redemptionDslRepository.findOne(setup.getRedemption().getId());

    assertUsed(ride, redemption, 1);
  }
}
