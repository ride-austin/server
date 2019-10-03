package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.test.fixtures.AdministratorFixture;

public abstract class BaseSurgeRedispatchTestSetup<T extends SetupAction<T>> extends BaseRedispatchTestSetup<T> {

  @Inject
  private AdministratorFixture administratorFixture;

  private Administrator administrator;
  protected SurgeArea surgeArea;

  @Override
  @Transactional
  public T setUp() throws Exception {
    this.administrator = administratorFixture.getFixture();
    return super.setUp();
  }

  public Administrator getAdministrator() {
    return administrator;
  }

  public SurgeArea getSurgeArea() {
    return surgeArea;
  }
}
