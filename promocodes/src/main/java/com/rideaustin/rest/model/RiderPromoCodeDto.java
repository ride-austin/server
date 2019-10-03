package com.rideaustin.rest.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@ApiModel
@JsonDeserialize(builder = RiderPromoCodeDto.RiderPromoCodeDtoBuilder.class)
public class RiderPromoCodeDto {

  @ApiModelProperty(required = true)
  private final String codeLiteral;
  @ApiModelProperty(required = true)
  private final BigDecimal codeValue;
  @ApiModelProperty(required = true)
  private final Long maximumRedemption;
  @ApiModelProperty(required = true)
  private final Long currentRedemption;
  @ApiModelProperty(required = true)
  private final String detailText;
  @ApiModelProperty(required = true)
  private final String smsBody;
  @ApiModelProperty(required = true)
  private final String emailBody;
  @Setter
  @ApiModelProperty(required = true)
  private BigDecimal remainingCredit;

  @JsonPOJOBuilder(withPrefix = "")
  public static class RiderPromoCodeDtoBuilder {}
}