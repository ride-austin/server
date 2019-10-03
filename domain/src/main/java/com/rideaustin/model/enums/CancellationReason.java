package com.rideaustin.model.enums;

public enum CancellationReason {
  CHANGE_BOOKING("I needed to change booking (time/place)"),
  CHANGE_MIND("I changed my mind"),
  ANOTHER_RIDE("I found another ride"),
  MISTAKE("I booked by mistake"),
  TOO_LONG("It was too long to wait"),
  RIDER_RESERVED_1(""),
  NO_SHOW("Rider did not show up"),
  WRONG_GPS("Wrong GPS location"),
  TOO_MANY_RIDERS("Too many riders for car type"),
  DRIVER_RESERVED_1(""),
  OTHER("Other")
  ;

  private final String description;

  CancellationReason(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
