package com.rideaustin.model.enums;

public enum PaymentProvider {
  CREDIT_CARD(null, null),
  APPLE_PAY("Apple Pay", "");

  private String name;
  private String icon;

  PaymentProvider(String name, String iconUrl) {
    this.name = name;
    this.icon = iconUrl;
  }

  public String getIcon() {
    return icon;
  }

  public String getName() {
    return name;
  }
}
