package com.rideaustin.service.promocodes;

import static com.rideaustin.service.promocodes.AbstractPromocodeTest.assertNotUsed;
import static com.rideaustin.service.promocodes.AbstractPromocodeTest.assertUsed;
import static org.assertj.core.api.Assertions.assertThat;

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
import com.rideaustin.test.setup.C1177051Setup;
import com.rideaustin.testrail.TestCases;

@Category(RiderPromocode.class)
public class C1177051IT extends AbstractNonTxPromocodeTest<C1177051Setup> {

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
  @TestCases("C1177051")
  public void shouldUseOnlyOnePromocodePerRide() throws Exception {
    final Rider rider = setup.getRider();
    final ActiveDriver activeDriver = setup.getActiveDriver();
    final LatLng center = locationProvider.getCenter();

    final Long rideId = rideAction.performRide(rider, center, center, activeDriver);

    final Ride ride = rideDslRepository.findOne(rideId);

    PromocodeRedemption firstRedemption = redemptionDslRepository.findOne(setup.getRedemption().getId());
    PromocodeRedemption secondRedemption = redemptionDslRepository.findOne(setup.getSecondRedemption().getId());

    assertThat(ride.getFreeCreditCharged().getAmount()).isEqualByComparingTo(BigDecimal.ONE);
    assertUsed(ride, firstRedemption, 1);
    assertNotUsed(ride, BigDecimal.ONE, secondRedemption, 0);
  }
}
