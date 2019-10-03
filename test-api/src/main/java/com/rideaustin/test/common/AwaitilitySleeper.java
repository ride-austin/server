package com.rideaustin.test.common;

import java.time.Instant;

import com.jayway.awaitility.Awaitility;

public class AwaitilitySleeper implements Sleeper {
  @Override
  public void sleep(long milliSeconds) {
    Instant start = Instant.now();
    Awaitility.await().forever().until(() ->
      Instant.now().getEpochSecond() - start.getEpochSecond() > milliSeconds / 1000
    );
  }
}
