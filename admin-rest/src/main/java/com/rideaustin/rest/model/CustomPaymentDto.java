package com.rideaustin.rest.model;

import javax.persistence.Convert;

import org.joda.money.Money;

import com.rideaustin.model.enums.CustomPaymentCategory;
import com.rideaustin.model.helper.MoneyConverter;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@ApiModel
@RequiredArgsConstructor
public class CustomPaymentDto {

  @ApiModelProperty(required = true, example = "1")
  private final long id;

  @ApiModelProperty(required = true, example = "1")
  private final long driverId;

  @ApiModelProperty(required = true)
  private final String driverFirstName;

  @ApiModelProperty(required = true)
  private final String driverLastName;

  @ApiModelProperty(required = true)
  private final String driverEmail;

  @ApiModelProperty(required = true, example = "1")
  private final long creatorId;

  @ApiModelProperty(required = true)
  private final String creatorFirstName;

  @ApiModelProperty(required = true)
  private final String creatorLastName;

  @ApiModelProperty(required = true)
  private final String creatorEmail;

  @Setter
  @ApiModelProperty(required = true)
  private String paymentDate;

  @ApiModelProperty(required = true)
  @Convert(converter = MoneyConverter.class)
  private final Money value;

  @ApiModelProperty(required = true)
  private final CustomPaymentCategory category;

  @ApiModelProperty
  private final String description;

}
