package com.rideaustin.rest.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.rideaustin.model.enums.CardBrand;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel
@JsonDeserialize(builder = RiderCardDto.RiderCardDtoBuilder.class)
public class RiderCardDto {

  @ApiModelProperty(required = true, example = "1")
  private final long id;
  @ApiModelProperty(required = true)
  private final String cardNumber;
  @ApiModelProperty(required = true)
  private final CardBrand cardBrand;
  @ApiModelProperty(required = true)
  private final boolean cardExpired;
  @ApiModelProperty(required = true)
  private final boolean primary;
  @ApiModelProperty(required = true)
  private final String expirationMonth;
  @ApiModelProperty(required = true)
  private final String expirationYear;

  @JsonPOJOBuilder(withPrefix = "")
  public static class RiderCardDtoBuilder {}

}
