package com.rideaustin.test.setup;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RA12423Setup extends AbstractSplitFarePromocodeApplicableToFeesSetup<RA12423Setup> {

  @Override
  @Transactional
  public RA12423Setup setUp() throws Exception {
    this.rider = riderFixture.getFixture();
    this.activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture)).getFixture();
    this.promocode = promocodeFixtureProvider.create(12.75, null,
      b -> b.applicableToFees(true)
        .carTypeBitMask(31)
    ).getFixture();
    return this;
  }
}
