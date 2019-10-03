package com.rideaustin.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.PromocodeDto;

public class PromocodeBuilder {

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private Promocode promocode = new Promocode();

  @Nonnull
  public static PromocodeBuilder create() {
    PromocodeBuilder builder = new PromocodeBuilder();
    builder.promocode.setId(new Random().nextLong());
    return builder;
  }

  public static PromocodeBuilder create(String literal) {
    PromocodeBuilder builder = create();
    builder.promocode.setCodeLiteral(literal);
    return builder;
  }

  public PromocodeBuilder setValue(Double val) {
    promocode.setCodeValue(BigDecimal.valueOf(val));
    return this;
  }

  public PromocodeBuilder setParams(boolean newRiderOnly) {
    promocode.setNewRidersOnly(newRiderOnly);
    return this;
  }

  public PromocodeBuilder setMaxRedemption(long maxRedemption) {
    promocode.setMaximumRedemption(maxRedemption);
    return this;
  }

  public PromocodeBuilder setDatesRange(String date1, String date2) throws ParseException {
    promocode.setStartsOn(dateFormat.parse(date1));
    promocode.setEndsOn(dateFormat.parse(date2));
    return this;
  }

  public PromocodeBuilder setValidityParams(String date, Integer period) throws ParseException {
    if (date != null) {
      promocode.setUseEndDate(dateFormat.parse(date));
    }
    promocode.setValidForNumberOfDays(period);
    return this;
  }

  public PromocodeBuilder setCurrentRedemption(long currentRedemption) {
    promocode.setCurrentRedemption(currentRedemption);
    return this;
  }

  public PromocodeBuilder setOwner(Rider owner) {
    promocode.setOwner(owner);
    return this;
  }

  public PromocodeBuilder setCities(Integer cityBitMask) {
    promocode.setCityBitmask(cityBitMask);
    return this;
  }

  public PromocodeBuilder setCarTypes(Integer carTypes) {
    promocode.setCarTypeBitmask(carTypes);
    return this;
  }

  public PromocodeDto asDto() {
    return asDto(Collections.emptySet(), Collections.emptySet());
  }

  public PromocodeDto asDto(Set<String> carTypes, Set<Long> cities) {
    return PromocodeDto.builder()
      .id(promocode.getId())
      .promocodeType(promocode.getPromocodeType())
      .codeLiteral(promocode.getCodeLiteral())
      .codeValue(promocode.getCodeValue())
      .startsOn(promocode.getStartsOn())
      .endsOn(promocode.getEndsOn())
      .newRidersOnly(promocode.isNewRidersOnly())
      .maximumRedemption(promocode.getMaximumRedemption())
      .maximumUsesPerAccount(promocode.getMaximumUsesPerAccount())
      .currentRedemption(promocode.getCurrentRedemption())
      .driverId(promocode.getDriverId())
      .carTypes(carTypes)
      .cities(cities)
      .nextTripOnly(promocode.isNextTripOnly())
      .useEndDate(promocode.getUseEndDate())
      .validForNumberOfDays(promocode.getValidForNumberOfDays())
      .validForNumberOfRides(promocode.getValidForNumberOfRides())
      .validForNumberOfRides(promocode.getValidForNumberOfRides())
      .maxPromotionValue(promocode.getMaximumPromotionValue())
      .build();
  }

  public Promocode get() {
    return promocode;
  }
}
