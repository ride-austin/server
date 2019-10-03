package com.rideaustin.test.setup;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class C1307548Setup extends BaseC1307548_53Setup<C1307548Setup> {

  @Override
  @Transactional
  public C1307548Setup setUp() throws Exception {
    this.promocode = promocodeFixture.getFixture();
    this.rider = riderFixture.getFixture();
    this.activeDriver = activeDriverFixtureProvider.create().getFixture();
    return this;
  }

}
