package com.rideaustin.rest.model;

import static com.rideaustin.Constants.ROUNDING_MODE;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.model.user.Gender;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel
@AllArgsConstructor
public class SimpleDriverDto {

  @ApiModelProperty(required = true)
  private final long userId;
  @ApiModelProperty(required = true)
  private final String firstName;
  @ApiModelProperty(required = true)
  private final String lastName;
  @ApiModelProperty(required = true)
  private final String phoneNumber;
  @ApiModelProperty(required = true)
  private final String email;
  @ApiModelProperty(required = true)
  private final Boolean active;
  @ApiModelProperty(required = true)
  private final Boolean enabled;
  @ApiModelProperty(required = true)
  private final long driverId;
  @ApiModelProperty
  private final String driverPicture;
  @ApiModelProperty
  private final String userPicture;
  @ApiModelProperty(required = true)
  private final String driverLicensePicture;
  @ApiModelProperty(required = true)
  private final List<SimpleCarDto> cars;
  @ApiModelProperty(required = true)
  private final Set<String> driverTypes;
  @ApiModelProperty(required = true)
  private final Gender gender;
  @ApiModelProperty(required = true)
  private final Double rating;
  @ApiModelProperty(required = true)
  private final PayoneerStatus payoneerStatus;
  @ApiModelProperty(required = true)
  private final CityApprovalStatus cityApprovalStatus;
  @ApiModelProperty(required = true)
  private final DriverActivationStatus activationStatus;
  @ApiModelProperty(required = true)
  private final DocumentStatus driverLicenseStatus;
  @ApiModelProperty(required = true)
  private final DocumentStatus profilePhotosStatus;
  @ApiModelProperty(required = true)
  private final DriverOnboardingStatus onboardingStatus;
  @ApiModelProperty
  private final Date lastLoginDate;

  @JsonProperty("ratingAverage")
  @ApiModelProperty(required = true)
  public BigDecimal getRatingAverage() {
    return rating == null ? null : BigDecimal.valueOf(rating).setScale(2, ROUNDING_MODE);
  }



}
