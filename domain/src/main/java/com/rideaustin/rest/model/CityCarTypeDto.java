package com.rideaustin.rest.model;

import java.math.BigDecimal;

import org.joda.money.Money;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel
@AllArgsConstructor
@JsonDeserialize(builder = CityCarTypeDto.CityCarTypeDtoBuilder.class)
public class CityCarTypeDto {

  @ApiModelProperty(required = true)
  private final String carCategory;
  @ApiModelProperty(required = true)
  private final String title;
  @ApiModelProperty(required = true)
  private final String description;
  @ApiModelProperty(required = true)
  private final String iconUrl;
  @ApiModelProperty(required = true)
  private final String plainIconUrl;
  @ApiModelProperty(required = true)
  private final String mapIconUrl;
  @ApiModelProperty(required = true)
  private final String fullIconUrl;
  @ApiModelProperty(required = true)
  private final String unselectedIconUrl;
  @ApiModelProperty(required = true)
  private final String selectedIconUrl;
  @ApiModelProperty(required = true)
  private final String selectedFemaleIconUrl;
  @ApiModelProperty(required = true)
  private final String configuration;
  @ApiModelProperty(required = true, example = "4")
  private final Integer maxPersons;
  @ApiModelProperty(required = true, example = "1")
  private final Integer order;
  @ApiModelProperty(required = true)
  private final Boolean active;
  @ApiModelProperty(required = true, example = "1")
  private final Long cityId;
  @ApiModelProperty(required = true)
  private final Money minimumFare;
  @ApiModelProperty(required = true)
  private final Money baseFare;
  @ApiModelProperty(required = true)
  private final Money bookingFee;
  @ApiModelProperty(required = true)
  private final Money raFixedFee;
  @ApiModelProperty(required = true)
  private final Money ratePerMile;
  @ApiModelProperty(required = true)
  private final Money ratePerMinute;
  @ApiModelProperty(required = true)
  private final Money cancellationFee;
  @ApiModelProperty(required = true)
  private final BigDecimal tncFeeRate;
  @ApiModelProperty(required = true)
  private final BigDecimal processingFeeRate;
  @ApiModelProperty(required = true)
  private final String processingFeeText;
  @ApiModelProperty(required = true)
  private final String processingFee;

  @JsonPOJOBuilder(withPrefix = "")
  public static class CityCarTypeDtoBuilder {}
}