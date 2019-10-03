package com.rideaustin.rest.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
public class ListPromocodeDto {

  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final String title;
  @ApiModelProperty(required = true)
  private final String codeLiteral;
  @ApiModelProperty(required = true)
  private final BigDecimal codeValue;
  @ApiModelProperty(required = true)
  private final Date startsOn;
  @ApiModelProperty(required = true)
  private final Date endsOn;
  @ApiModelProperty(required = true)
  private final boolean newRidersOnly;
  @ApiModelProperty(required = true)
  private final Long maximumRedemption;
  @ApiModelProperty(required = true)
  private final Long currentRedemption;
  @ApiModelProperty(required = true)
  private final Integer maximumUsesPerAccount;
  @Setter
  @ApiModelProperty(required = true)
  private Long usageCount = 0L;
  @Setter
  @ApiModelProperty(required = true)
  private Set<Long> cities;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Integer cityBitmask;
  @Setter
  @ApiModelProperty(required = true)
  private Set<String> carTypes;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Integer carTypesBitmask;

  @ApiModelProperty(required = true)
  private final Integer validForNumberOfRides;
  @ApiModelProperty(required = true)
  private final Integer validForNumberOfDays;
  @ApiModelProperty(required = true)
  private final Date useEndDate;
  @ApiModelProperty(required = true)
  private final boolean nextTripOnly;
  @ApiModelProperty(required = true)
  private final boolean applicableToFees;
  @Setter
  @ApiModelProperty
  private BigDecimal maxPromotionValue;
  @ApiModelProperty(required = true)
  private final BigDecimal cappedAmountPerUse;

  @QueryProjection
  public ListPromocodeDto(long id, String title, String codeLiteral, BigDecimal codeValue, Date startsOn, Date endsOn,
    boolean newRidersOnly, Long maximumRedemption, Long currentRedemption, Integer maximumUsesPerAccount, Integer cityBitmask,
    Integer carTypesBitmask, Integer validForNumberOfRides, Integer validForNumberOfDays, Date useEndDate, boolean nextTripOnly,
    boolean applicableToFees, BigDecimal cappedAmountPerUse) {
    this.id = id;
    this.title = title;
    this.codeLiteral = codeLiteral;
    this.codeValue = codeValue;
    this.startsOn = startsOn;
    this.endsOn = endsOn;
    this.newRidersOnly = newRidersOnly;
    this.maximumRedemption = maximumRedemption;
    this.currentRedemption = currentRedemption;
    this.maximumUsesPerAccount = maximumUsesPerAccount;
    this.cityBitmask = cityBitmask;
    this.carTypesBitmask = carTypesBitmask;
    this.validForNumberOfRides = validForNumberOfRides;
    this.validForNumberOfDays = validForNumberOfDays;
    this.useEndDate = useEndDate;
    this.nextTripOnly = nextTripOnly;
    this.applicableToFees = applicableToFees;
    this.cappedAmountPerUse = cappedAmountPerUse;
  }
}
