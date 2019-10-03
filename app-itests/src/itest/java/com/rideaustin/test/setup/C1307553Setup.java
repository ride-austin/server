package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.repo.dsl.PromocodeDslRepository;

@Component
public class C1307553Setup extends BaseC1307548_53Setup<C1307553Setup> {

  @Inject
  private PromocodeDslRepository repository;

  @Override
  @Transactional
  public C1307553Setup setUp() throws Exception {
    this.rider = riderFixture.getFixture();
    this.activeDriver = activeDriverFixtureProvider.create().getFixture();
    this.promocode = promocodeFixture.getFixture();
    promocode.setApplicableToFees(true);
    promocode = repository.save(promocode);
    return this;
  }

}
