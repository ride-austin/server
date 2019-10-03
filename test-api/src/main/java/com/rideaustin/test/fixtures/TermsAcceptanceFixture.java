package com.rideaustin.test.fixtures;

import com.rideaustin.model.TermsAcceptance;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.TermsDslRepository;
import com.rideaustin.test.fixtures.check.TermAcceptanceChecker;

public class TermsAcceptanceFixture extends AbstractFixture<TermsAcceptance> {

  private Driver driver;
  private final TermsDslRepository repository;

  public TermsAcceptanceFixture(TermsDslRepository repository) {
    this.repository = repository;
    setRecordChecker(new TermAcceptanceChecker(repository));
  }

  @Override
  protected TermsAcceptance createObject() {
    return TermsAcceptance.builder()
      .driver(driver)
      .terms(repository.getOne(1L))
      .build();
  }

  public void setDriver(Driver driver) {
    this.driver = driver;
  }

}
