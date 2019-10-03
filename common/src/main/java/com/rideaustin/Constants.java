package com.rideaustin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public final class Constants {

  public static final String DEFAULT_DRIVER_PHOTO = "";

  // provides city IDs to be used where necessary
  // (business logic specific for city, unit tests etc)
  public enum City {
    // actual IDs from the DB
    AUSTIN(1L),
    HOUSTON(2L);

    private final Long id;

    City(Long id) {
      this.id = id;
    }

    public static City getByCityId(Long cityId) {

      for (City city : values()) {
        if (city.getId().equals(cityId)) {
          return city;
        }
      }
      throw new IllegalArgumentException(String.format("city not found by id %s", cityId));
    }

    @Override
    public String toString() {
      return name() + "[" + id + ']';
    }

    public Long getId() {
      return this.id;
    }
  }

  public static final BigDecimal MILES_PER_METER = BigDecimal.valueOf(1d / 1609.34d);

  public static final BigDecimal MINUTES_PER_SECOND = BigDecimal.valueOf(1d / 60d);

  public static final BigDecimal MINUTES_PER_MILLISECOND = BigDecimal.valueOf(1d / 60000d);

  public static final BigDecimal HOURS_PER_MILLISECOND = BigDecimal.valueOf(1d / 3600000d);

  public static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(60 * 60L);

  public static final BigDecimal SECONDS_PER_DAY = BigDecimal.valueOf(24 * 60 * 60L);

  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

  public static final Money ZERO_USD = Money.zero(CurrencyUnit.USD);

  public static final Money MINIMUM_STRIPE_CHARGE = Money.of(CurrencyUnit.USD, BigDecimal.valueOf(0.5));

  public static final int PAGE_SIZE = 25;

  // Rates as provided by Andy (RA-46)

  public static final Money MAXIMUM_DISTANCE_FARE = Money.of(CurrencyUnit.USD, 1000d);

  public static final String RIDE_MAP_FOLDER = "ride-maps";

  public static final Integer EARTH_RADIUS = 6367000;

  public static final int SQUARE_MILE_DIMENSION_MILES = 10;

  // Convert to radians
  public static final double TO_RADIANS = Math.PI / 180;

  // Max number of closest active drivers to return
  public static final int CLOSEST_ACDR_SIZE = 10;

  public static final ZoneId CST_ZONE = ZoneId.of("US/Central");

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy").withZone(CST_ZONE);

  public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a").withZone(CST_ZONE);

  public static final long DEFAULT_CITY_ID = City.AUSTIN.getId();

  public static final BigDecimal NEUTRAL_SURGE_FACTOR = BigDecimal.ONE;

  public static final String NEUTRAL_SURGE_FACTOR_STR = "1";

  public static final String MAXIMUM_SURGE_FACTOR_STR = "8";

  public static final String ENCODING_UTF8 = "UTF-8";

  private Constants() {
  }

  public static class ErrorMessages {

    public static final String PHONE_NUMBER_REQUIRED = "Phone number is required!";

    public static final String PHONE_NUMBER_NO_VOIP = "VoIP numbers are not allowed!";

    public static final String DEVICE_IS_BLOCKED = "Your account is blocked by administrator. Please contact Ride Austin support.";

    public static final String EMAIL_NOT_VALID = "Valid email is required!";

    public static final String FIRST_NAME_REQUIRED = "Firstname is required!";

    public static final String LAST_NAME_REQUIRED = "Lastname is required!";

    public static final String PASS_REQUIRED = "Password is required!";

    public static final String USER_ALREADY_EXISTS = "User already exists!";

    public static final String TERM_DRIVER_ACCEPTED_ALREADY = "This terms has already been accepted!";

    public static final String TERM_NOT_FOUND_TEMPLATE = "There is no term with id {%s}";

    private ErrorMessages() {
    }
  }

  public static class Configuration {

    public static final String LOOKUP_SERVICE_MOCK = "mock";

    public static final String LOOKUP_SERVICE_TWILIO = "twilio";

    private Configuration() {
    }
  }

  public static class EmailTitle {

    public static final String RIDER_SIGNUP_EMAIL = "Thank you for signing up!!!";
    public static final String PASSWORD_RESET_EMAIL = "Your password has been reset";
    public static final String DRIVER_SIGNUP_EMAIL = ": Important Driver Sign Up Instructions";
    public static final String USER_IS_ACTIVATED_EMAIL = "Your account is activated";
    public static final String USER_IS_DEACTIVATED_EMAIL = "Your account is deactivated";
    public static final String USER_IS_ENABLED_EMAIL = "Your account is enabled";
    public static final String USER_IS_DISABLED_EMAIL = "Your account is disabled";
    public static final String END_RIDE_EMAIL = "Your trip with ";
    public static final String USER_CARD_IS_LOCKED_EMAIL = "Your credit card is locked";
    public static final String INVALID_PAYMENT_EMAIL = "Payment was declined";
    public static final String RIDE_CANCELLATION_EMAIL = "Your ride was canceled";

    private EmailTitle() {

    }
  }

}
