package com.rideaustin.service.promocodes;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PromocodeServiceConfig {

  private static final BigDecimal MAXIMUM_PROMOCODE_VALUE = BigDecimal.valueOf(50.00);
  private static final BigDecimal DEFAULT_USER_PROMOCODE_VALUE = BigDecimal.valueOf(10);
  private static final long DEFAULT_USER_MAX_REDEMPTION = 20L;
  private static final BigDecimal MAXIMUM_ACTIVE_REDEEMED_CREDIT = BigDecimal.valueOf(500.00);
  private static final Integer USER_PROMOCODE_VALIDITY_PERIOD = 90;

  private final BigDecimal userPromocodeFreeCreditAmount;
  private final Long userPromocodeMaxRedemption;
  private final Integer userPromoCodesValidityPeriod;
  private final BigDecimal maximumPromocodeValue;
  private final BigDecimal maximumActiveRedeemedCredit;

  @Inject
  public PromocodeServiceConfig(Environment environment) {

    userPromocodeFreeCreditAmount = environment.getProperty("promocode.default.ridercode.credit",
      BigDecimal.class, DEFAULT_USER_PROMOCODE_VALUE);
    userPromocodeMaxRedemption = environment.getProperty("promocode.default_rider_max_redemptions",
      Long.class, DEFAULT_USER_MAX_REDEMPTION);
    maximumActiveRedeemedCredit = environment.getProperty("promocode.maximum_active_redeemed_credit",
      BigDecimal.class, MAXIMUM_ACTIVE_REDEEMED_CREDIT);
    userPromoCodesValidityPeriod = environment.getProperty("promocode.user_promocode_validity_period",
      Integer.class, USER_PROMOCODE_VALIDITY_PERIOD);
    maximumPromocodeValue = environment.getProperty("promocode.maximum_promocode_value",
      BigDecimal.class, MAXIMUM_PROMOCODE_VALUE);

  }

  public BigDecimal getUserPromocodeFreeCreditAmount() {
    return userPromocodeFreeCreditAmount;
  }

  public Long getUserPromocodeMaxRedemption() {
    return userPromocodeMaxRedemption;
  }

  public Integer getUserPromoCodesValidityPeriod() {
    return userPromoCodesValidityPeriod;
  }

  public BigDecimal getMaximumPromocodeValue() {
    return maximumPromocodeValue;
  }

  public BigDecimal getMaximumActiveRedeemedCredit() {
    return maximumActiveRedeemedCredit;
  }
}
