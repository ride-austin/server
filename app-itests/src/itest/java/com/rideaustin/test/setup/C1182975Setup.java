package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.test.fixtures.SurgeAreaFixture;

@Component
public class C1182975Setup extends BaseSurgeRedispatchTestSetup<C1182975Setup> {

  @Inject
  @Named("doubleSurgeAreaFixture")
  private SurgeAreaFixture surgeAreaFixture;

  @Override
  @Transactional
  public C1182975Setup setUp() throws Exception {
    surgeArea = surgeAreaFixture.getFixture();
    return super.setUp();
  }

  @Override
  protected C1182975Setup getThis() {
    return this;
  }
}
