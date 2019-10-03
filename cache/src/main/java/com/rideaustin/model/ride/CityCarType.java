package com.rideaustin.model.ride;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.JsonConfigurable;
import com.rideaustin.model.enums.ConfigurationWeekday;
import com.rideaustin.model.helper.MoneyConverter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "city_car_types")
public class CityCarType extends BaseEntity implements JsonConfigurable<CityCarType.CityTypeConfiguration> {

  @Column(name = "city_id")
  private Long cityId;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "car_category")
  @JsonBackReference
  private CarType carType;

  @Column(name = "enabled")
  private boolean enabled;

  @Column(name = "configuration")
  private String configuration;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "minimum_fare")
  private Money minimumFare;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "base_fare")
  private Money baseFare;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "booking_fee")
  private Money bookingFee;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "rate_per_mile")
  private Money ratePerMile;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "rate_per_minute")
  private Money ratePerMinute;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "cancellation_fee")
  private Money cancellationFee;

  @Column(name = "city_fee_rate")
  private BigDecimal cityFeeRate;

  @Column(name = "processing_fee_rate")
  private BigDecimal processingFeeRate;

  @Column(name = "processing_fee_fixed_part")
  private BigDecimal processingFeeFixedPart;

  @Column(name = "processing_fee_minimum")
  @Convert(converter = MoneyConverter.class)
  private Money processingFeeMinimum;

  @Column(name = "processing_fee_text")
  private String processingFeeText;

  @Column(name = "fixed_ra_fee")
  @Convert(converter = MoneyConverter.class)
  private Money fixedRAFee;

  @Override
  public CityTypeConfiguration getDefaultConfiguration() {
    return new CityTypeConfiguration();
  }

  @Override
  public Class<CityTypeConfiguration> getConfigurationClass() {
    return CityTypeConfiguration.class;
  }

  @Override
  public CityTypeConfiguration getConfigurationObject(ObjectMapper objectMapper) {
    try {
      Map<String, Object> carTypeConfig = objectMapper.readValue(carType.getConfiguration(), carType.getConfigurationClass());
      Map<String, Object> cityConfig = objectMapper.readValue(configuration, getConfigurationClass());
      CityTypeConfiguration mergedConfig = new CityTypeConfiguration();
      mergedConfig.putAll(Optional.ofNullable(carTypeConfig).orElse(new HashMap<>()));
      mergedConfig.putAll(Optional.ofNullable(cityConfig).orElse(new HashMap<>()));
      return mergedConfig;
    } catch (IOException e) {
      log.error("Failed to load configuration", e);
      return getDefaultConfiguration();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public static class CityTypeConfiguration extends CarType.Configuration {
    public CityTypeConfiguration() {
      super();
    }

    public Set<ConfigurationWeekday> getWeekdays() {
      return ConfigurationWeekday.fromBitmask((Integer) get("activeWeekdays"));
    }

    public Integer getActiveFrom() {
      return (Integer) get("activeFrom");
    }

    public Integer getActiveTo() {
      return (Integer) get("activeTo");
    }
  }
}
