package com.rideaustin.rest.model;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class SimpleRiderDto {

  @ApiModelProperty(required = true)
  private final long userId;
  @ApiModelProperty(required = true)
  private final String firstName;
  @ApiModelProperty(required = true)
  private final String lastName;
  @ApiModelProperty(required = true)
  private final String email;
  @ApiModelProperty(required = true)
  private final String phoneNumber;
  @ApiModelProperty(required = true)
  private final boolean active;
  @ApiModelProperty(required = true)
  private final boolean enabled;
  @ApiModelProperty(required = true)
  private final long riderId;
  @ApiModelProperty(required = true)
  private final String riderPictureUrl;

  @QueryProjection
  public SimpleRiderDto(long userId, String firstName, String lastName, String email, String phoneNumber, boolean active,
    boolean enabled, long riderId, String riderPictureUrl) {
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.active = active;
    this.enabled = enabled;
    this.riderId = riderId;
    this.riderPictureUrl = riderPictureUrl;
  }
}
