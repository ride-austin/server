package com.rideaustin.test.fixtures.check;

import java.util.Optional;

import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DriverDslRepository;

public class DriverChecker implements RecordChecker<Driver> {
  private final DriverDslRepository driverDslRepository;

  public DriverChecker(DriverDslRepository driverDslRepository) {
    this.driverDslRepository = driverDslRepository;
  }

  @Override
  public Optional<Driver> getIfExists(Driver source) {
    return Optional.ofNullable(driverDslRepository.findByEmail(source.getEmail()));
  }
}
