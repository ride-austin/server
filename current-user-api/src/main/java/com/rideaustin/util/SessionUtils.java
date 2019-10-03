package com.rideaustin.util;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ApiClientAppType;

public final class SessionUtils {

  private SessionUtils() {}

  public static boolean checkToken(ApiClientAppType appType, String token, String sessionToken) {
    return !appType.isMobileApp() || sessionToken.equals(token);
  }

  public static String buildSessionKey(String userId, ApiClientAppType apiClientAppType) {
    return userId.concat(":").concat(apiClientAppType.name());
  }

  public static String buildSessionKey(Session session, ApiClientAppType apiClientAppType) {
    return buildSessionKey(String.valueOf(session.getUser().getId()), apiClientAppType);
  }

  public static boolean isLoginRequestFromMobile(String userAgentHeader) {
    return userAgentHeader.startsWith("RideAustin") || userAgentHeader.startsWith("RideHouston");
  }
}
