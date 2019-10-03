package com.rideaustin.service.model;

public enum Events {
  HANDSHAKE_REQUEST_SEND,
  DISPATCH_REQUEST_SEND,
  DISPATCH_REQUEST_REACH(false, false, true),
  HANDSHAKE_ACKNOWLEDGE(false, false, true),
  DISPATCH_REQUEST_ACCEPT(false, true, false),
  DISPATCH_REQUEST_DECLINE(false, false, true),
  ABORT_PREAUTHORIZATION_FAILED(false, true, true),
  UPDATE_DESTINATION,
  UPDATE_COMMENT,
  DRIVER_REACH(false, true, false),
  RIDER_CANCEL(false, true, false),
  ADMIN_CANCEL(false, true, false),
  DRIVER_CANCEL(true, true, false),
  FORCE_REDISPATCH(true, true, false),
  START_RIDE(false, true, false),
  NO_DRIVERS_AVAILABLE(false, true, false),
  END_RIDE(false, true, false);

  private boolean causesInception;
  private boolean killsInception;
  private boolean proxiedToInception;

  Events(boolean causesInception, boolean killsInception, boolean proxiedToInception) {
    this.causesInception = causesInception;
    this.killsInception = killsInception;
    this.proxiedToInception = proxiedToInception;
  }

  Events() {
    this.causesInception = false;
    this.killsInception = false;
    this.proxiedToInception = false;
  }

  public boolean causesInception() {
    return causesInception;
  }

  public boolean killsInception() {
    return killsInception;
  }

  public boolean isProxiedToInception() {
    return proxiedToInception;
  }
}
