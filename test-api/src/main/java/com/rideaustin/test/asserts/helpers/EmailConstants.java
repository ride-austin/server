package com.rideaustin.test.asserts.helpers;

public class EmailConstants {

  public static class TripSummary {

    public static final String PRIORITY_FARE = "Priority Fare";
    public static final String TIP = "Tip";
    public static final String ROUND_UP = "Charity Roundup";
    public static final String FARE_CREDIT = "Fare Credit";
    public static final String RIDE_CREDIT = "Ride Credit";
    public static final String CAR_TEMPLATE = "%s, %s mi";

    private TripSummary() {}
  }

  public static class Earnings {

    public static final String EARNINGS_TITLE_PREFIX = "Daily earnings report for";
    public static final String CUSTOM_EARNINGS_TITLE_PREFIX = "Additional earnings report for";
    public static final String EARNINGS_HEADER = "Your earnings for the week";
    public static final String CUSTOM_EARNINGS_HEADER = "Your additional earnings for the week";

    private Earnings() {}
  }

  private EmailConstants() {}
}