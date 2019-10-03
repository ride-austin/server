package com.rideaustin.model.enums;

public enum CardBrand {
  VISA(""),
  MASTERCARD(""),
  AMERICAN_EXPRESS(""),
  DISCOVER(""),
  JCB(""),
  DINERS_CLUB(""),
  UNKNOWN("");
  private final String imageURL;

  CardBrand(String imageURL) {
    this.imageURL = imageURL;
  }

  public String imageURL() {
    return imageURL;
  }
}
