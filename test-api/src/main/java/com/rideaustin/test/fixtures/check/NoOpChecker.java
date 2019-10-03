package com.rideaustin.test.fixtures.check;

import java.util.Optional;

public class NoOpChecker<T> implements RecordChecker<T> {

  @Override
  public Optional<T> getIfExists(T source) {
    return Optional.empty();
  }
}
