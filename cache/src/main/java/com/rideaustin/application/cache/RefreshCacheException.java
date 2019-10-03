package com.rideaustin.application.cache;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RefreshCacheException extends Exception {
  public RefreshCacheException(Exception cause) {
    super(cause);
  }

}
