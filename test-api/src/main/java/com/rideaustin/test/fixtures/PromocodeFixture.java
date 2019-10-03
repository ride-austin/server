package com.rideaustin.test.fixtures;

import java.math.BigDecimal;
import java.util.Optional;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.promocodes.PromocodeType;
import com.rideaustin.utils.RandomString;

public class PromocodeFixture extends AbstractFixture<Promocode> {

  private RiderFixture riderFixture;

  private final Double cappedAmount;

  private final Double value;

  private final Integer cityBitMask;

  private final Integer carTypeBitMask;

  private final boolean nextTripOnly;
  
  private final boolean applicableToFees;

  private final Integer maxUsePerAccount;

  private final boolean newRidersOnly;

  PromocodeFixture(RiderFixture riderFixture) {
    this(riderFixture, null, null, null);
  }

  PromocodeFixture(RiderFixture riderFixture, Double value, Integer cityBitMask, Integer carTypeBitMask) {
    this(riderFixture, value, cityBitMask, carTypeBitMask, false, false, null, null, false);
  }

  PromocodeFixture(RiderFixture riderFixture, Double value, Integer cityBitMask, Integer carTypeBitMask,
    boolean nextTripOnly, boolean applicableToFees, Integer maxUsePerAccount, Double cappedAmount, boolean newRidersOnly) {
    this.riderFixture = riderFixture;
    this.value = value;
    this.cityBitMask = cityBitMask;
    this.carTypeBitMask = carTypeBitMask;
    this.nextTripOnly = nextTripOnly;
    this.applicableToFees = applicableToFees;
    this.maxUsePerAccount = maxUsePerAccount;
    this.cappedAmount = cappedAmount;
    this.newRidersOnly = newRidersOnly;
  }

  public static PromocodeFixtureBuilder builder() {
    return new PromocodeFixtureBuilder();
  }

  @Override
  protected Promocode createObject() {
    Promocode promocode = Promocode.builder()
      .codeValue(Optional.ofNullable(value).map(BigDecimal::new).orElse(BigDecimal.ZERO))
      .codeLiteral(RandomString.generate(5))
      .cityBitmask(Optional.ofNullable(cityBitMask).orElse(1))
      .carTypeBitmask(Optional.ofNullable(carTypeBitMask).orElse(1))
      .nextTripOnly(nextTripOnly)
      .applicableToFees(applicableToFees)
      .maximumUsesPerAccount(Optional.ofNullable(maxUsePerAccount).orElse(10))
      .cappedAmountPerUse(Optional.ofNullable(cappedAmount).map(BigDecimal::valueOf).orElse(null))
      .newRidersOnly(newRidersOnly)
      .build();
    if (riderFixture != null) {
      promocode.setPromocodeType(PromocodeType.USER);
      promocode.setOwner(riderFixture.getFixture());
    } else {
      promocode.setPromocodeType(PromocodeType.PUBLIC);
    }
    return promocode;
  }

  public static class PromocodeFixtureBuilder {
    private RiderFixture riderFixture;

    private Double value;

    private Integer cityBitMask;

    private Integer carTypeBitMask;

    private boolean nextTripOnly = false;
    
    private boolean applicableToFees = false;

    private Integer maxUsePerAccount;

    private Double cappedAmount;

    private boolean newRidersOnly = false;

    public PromocodeFixtureBuilder riderFixture(RiderFixture riderFixture) {
      this.riderFixture = riderFixture;
      return this;
    }

    public PromocodeFixtureBuilder value(Double value) {
      this.value = value;
      return this;
    }

    public PromocodeFixtureBuilder cityBitMask(Integer cityBitMask) {
      this.cityBitMask = cityBitMask;
      return this;
    }

    public PromocodeFixtureBuilder carTypeBitMask(Integer carTypeBitMask) {
      this.carTypeBitMask = carTypeBitMask;
      return this;
    }

    public PromocodeFixtureBuilder nextTripOnly(boolean nextTripOnly) {
      this.nextTripOnly = nextTripOnly;
      return this;
    }

    public PromocodeFixtureBuilder applicableToFees(boolean applicableToFees) {
      this.applicableToFees = applicableToFees;
      return this;
    }

    public PromocodeFixtureBuilder maxUsePerAccount(Integer maxUsePerAccount) {
      this.maxUsePerAccount = maxUsePerAccount;
      return this;
    }

    public PromocodeFixtureBuilder cappedAmountPerUse(double cappedAmount) {
      this.cappedAmount = cappedAmount;
      return this;
    }

    public PromocodeFixtureBuilder newRidersOnly(boolean newRidersOnly) {
      this.newRidersOnly = newRidersOnly;
      return this;
    }

    public PromocodeFixture build() {
      return new PromocodeFixture(riderFixture, value, cityBitMask, carTypeBitMask, nextTripOnly, applicableToFees,
        maxUsePerAccount, cappedAmount, newRidersOnly);
    }
  }
}
