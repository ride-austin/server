package com.rideaustin.rest.model;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.util.Map;

import com.rideaustin.model.enums.DriverOnboardingStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class DriverStatusDto {

  @ApiModelProperty
  private Long active;
  @ApiModelProperty
  private Long rejected;
  @ApiModelProperty
  private Long suspended;
  @ApiModelProperty
  private Long pending;
  @ApiModelProperty
  private Long finalReview;

  public DriverStatusDto(){}

  public DriverStatusDto(Map<DriverOnboardingStatus, Long> statusMap) {
    this.active = safeZero(statusMap.get(DriverOnboardingStatus.ACTIVE));
    this.pending = safeZero(statusMap.get(DriverOnboardingStatus.PENDING));
    this.rejected = safeZero(statusMap.get(DriverOnboardingStatus.REJECTED));
    this.suspended = safeZero(statusMap.get(DriverOnboardingStatus.SUSPENDED));
    this.finalReview = safeZero(statusMap.get(DriverOnboardingStatus.FINAL_REVIEW));
  }

  public Long getActive() {
    return active;
  }

  public Long getRejected() {
    return rejected;
  }

  public Long getSuspended() {
    return suspended;
  }

  public Long getPending() {
    return pending;
  }

  public Long getFinalReview() {
    return finalReview;
  }
}
