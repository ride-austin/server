package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.user.RiderCard;
import com.rideaustin.test.fixtures.CardFixture;

@Component
public class C1181787Setup extends BaseApplePaySetup<C1181787Setup> {

  @Inject
  private CardFixture otherCardFixture;
  private RiderCard otherCard;

  @Override
  @Transactional
  public C1181787Setup setUp() throws Exception {
    super.setUp();
    otherCardFixture.setRider(getRider());
    otherCard = otherCardFixture.getFixture();
    return this;
  }

  @Override
  protected C1181787Setup getThis() {
    return this;
  }

  public RiderCard getOtherCard() {
    return otherCard;
  }
}
