package com.rideaustin.model.enums;

public enum ApiClientAppType {

  MOBILE_DRIVER,
  MOBILE_RIDER,
  MOBILE_DISPATCHER,
  OTHER;

  public static ApiClientAppType getSuggestedAvatarTypeFromHeader(String userAgentHeader) {
    if (userAgentHeader != null &&
      (userAgentHeader.startsWith("RideAustin") || userAgentHeader.startsWith("RideHouston"))) {
      if (userAgentHeader.toLowerCase().contains("driver")) {
        return MOBILE_DRIVER;
      } else if (userAgentHeader.toLowerCase().contains("dispatcher")) {
        return MOBILE_DISPATCHER;
      } else {
        return MOBILE_RIDER;
      }
    } else {
      return OTHER;
    }
  }

  public boolean isMobileApp() {
    return this == MOBILE_DRIVER || this == MOBILE_RIDER;
  }

}
