package com.rideaustin.dispatch;

import org.slf4j.Logger;

import com.rideaustin.service.model.context.RideRequestContext;

public final class LogUtil {

  private static final String DISPATCH_LOG_MARKER = "[DISPATCH %s] %s";

  private LogUtil(){}

  public static void dispatchInfo(Logger logger, RideRequestContext requestContext, String message) {
    if (logger.isInfoEnabled()) {
      logger.info(String.format(DISPATCH_LOG_MARKER, requestContext != null ? requestContext.getRideId() : "", message));
    }
  }

  public static void dispatchInfo(Logger logger, Long rideId, String message) {
    if (logger.isInfoEnabled()) {
      logger.info(String.format(DISPATCH_LOG_MARKER, rideId, message));
    }
  }

  public static void flowInfo(Logger logger, RideRequestContext requestContext, String message) {
    if (logger.isInfoEnabled()) {
      logger.info(String.format("[Ride %s] %s", requestContext != null ? requestContext.getRideId() : "", message));
    }
  }

  public static void flowInfo(Logger logger, Long rideId, String message) {
    if (logger.isInfoEnabled()) {
      logger.info(String.format("[Ride %s] %s", rideId, message));
    }
  }

  public static void flowError(Logger logger, RideRequestContext requestContext, String message, Throwable e) {
    if (logger.isErrorEnabled()) {
      logger.error(String.format(DISPATCH_LOG_MARKER, requestContext != null ? requestContext.getRideId() : "", message), e);
    }
  }
}
