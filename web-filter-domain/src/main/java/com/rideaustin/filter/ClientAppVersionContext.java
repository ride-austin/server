package com.rideaustin.filter;

import org.springframework.util.Assert;

public class ClientAppVersionContext {

  private static final ThreadLocal<ClientAppVersion> contextHolder = new ThreadLocal<>();

  private ClientAppVersionContext() {

  }

  public static ClientAppVersion getAppVersion() {
    return contextHolder.get();
  }

  public static void setClientAppVersion(ClientAppVersion contextObject) {
    Assert.notNull(contextObject, "contextObject cannot be null");
    contextHolder.set(contextObject);
  }

  public static void clearClientAppVersion() {
    contextHolder.remove();
  }

}
