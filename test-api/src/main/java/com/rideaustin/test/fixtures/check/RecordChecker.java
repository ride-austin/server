package com.rideaustin.test.fixtures.check;

import java.util.Optional;

public interface RecordChecker<T> {
  Optional<T> getIfExists(T source);
}
