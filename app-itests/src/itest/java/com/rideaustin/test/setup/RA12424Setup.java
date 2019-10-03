package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;

@Component
public class RA12424Setup extends AbstractSplitFarePromocodeApplicableToFeesSetup<RA12424Setup> {

  @Inject
  private RiderFixtureProvider riderFixtureProvider;
  private Rider secondaryRider;

  @Override
  @Transactional
  public RA12424Setup setUp() throws Exception {
    this.rider = riderFixtureProvider.create().getFixture();
    this.secondaryRider = riderFixtureProvider.create().getFixture();
    this.activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture)).getFixture();
    this.promocode = promocodeFixtureProvider.create(12.75, null,
      b -> b.applicableToFees(true)
        .carTypeBitMask(31)
    ).getFixture();
    return this;
  }

  public Rider getSecondaryRider() {
    return secondaryRider;
  }
}
