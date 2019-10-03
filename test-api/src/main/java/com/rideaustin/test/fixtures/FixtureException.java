package com.rideaustin.test.fixtures;

public class FixtureException extends RuntimeException {

  public FixtureException() {
    super("Trying to access unfixed fixture!");
  }
}
