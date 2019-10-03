package com.rideaustin.utils;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

public final class FraudLogUtil {

  private FraudLogUtil(){}

  public static void fraudLog(Logger log, String message) {
    log.error(String.format("[FRAUD] %s", message));
  }

  public static String extractIPAddress(HttpServletRequest request) {
    if (request != null) {
      String remoteAddr = request.getHeader("X-FORWARDED-FOR");
      if (remoteAddr == null || "".equals(remoteAddr)) {
        remoteAddr = request.getRemoteAddr();
      }
      return remoteAddr;
    }
    return "";
  }
}
