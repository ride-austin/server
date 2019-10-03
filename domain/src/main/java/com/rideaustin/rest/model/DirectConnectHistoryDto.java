package com.rideaustin.rest.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.Constants;
import com.rideaustin.utils.DateUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
public class DirectConnectHistoryDto {

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final long driverId;
  @ApiModelProperty(required = true)
  private final String directConnectId;
  @ApiModelProperty(required = true)
  private final String driverFirstName;
  @ApiModelProperty(required = true)
  private final String driverLastName;
  @ApiModelProperty(required = true)
  private final String requestedAt;
  @Setter
  @ApiModelProperty(required = true)
  private String photoURL;

  @QueryProjection
  public DirectConnectHistoryDto(long driverId, String directConnectId, String driverFirstName, String driverLastName, Date requestedAt) {
    this.driverId = driverId;
    this.directConnectId = directConnectId;
    this.driverFirstName = driverFirstName;
    this.driverLastName = driverLastName;
    this.requestedAt = Constants.DATETIME_FORMATTER.format(DateUtils.dateToInstant(requestedAt));
  }
}
