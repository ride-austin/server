package com.rideaustin.test.setup;

public interface SetupAction<T> {
  T setUp() throws Exception;
}
