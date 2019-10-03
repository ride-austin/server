package com.rideaustin.model.enums;

public enum EventType {
  REQUESTED,
  HANDSHAKE,
  RIDER_CANCELLED,
  DRIVER_ASSIGNED,
  DRIVER_CANCELLED,
  DRIVER_REACHED,
  ACTIVE,
  NO_AVAILABLE_DRIVER,
  COMPLETED,
  ADMIN_CANCELLED,
  END_LOCATION_UPDATED,
  GO_OFFLINE,
  CUSTOM_MESSAGE,
  QUEUED_AREA_ENTERING,
  QUEUED_AREA_LEAVING("You have left the queue zone and have been removed from the queue"),
  QUEUED_AREA_LEAVING_INACTIVE("You have been removed from the queue because you were offline"),
  QUEUED_AREA_LEAVING_PENALTY("You have been removed from the queue due to 2 consequently missed requests. You will be unable to join the queue for next 15 minutes"),
  QUEUED_AREA_LEAVING_RIDE,
  QUEUED_AREA_UPDATE,
  SURGE_AREA_UPDATES,
  RATING_UPDATED,
  CAR_CATEGORY_CHANGE,
  DRIVER_TYPE_UPDATE,
  CONFIG_CREATED,
  CONFIG_CHANGED,
  RIDER_LOCATION_UPDATED,
  RIDER_COMMENT_UPDATED,
  RIDE_UPGRADE_ACCEPTED,
  RIDE_UPGRADE_DECLINED,
  RIDE_STACKED_REASSIGNED;

  private final String message;

  EventType() {
    this("");
  }

  EventType(String message) {
    this.message = message;
  }

  public static EventType from(RideStatus a) {
    return EventType.valueOf(a.name());
  }

  public String getMessage() {
    return message;
  }
}