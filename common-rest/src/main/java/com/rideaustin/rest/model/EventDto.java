package com.rideaustin.rest.model;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rideaustin.model.enums.EventType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
public class EventDto {

  @Setter
  @ApiModelProperty(required = true)
  private Long id;
  @ApiModelProperty
  private MobileDriverRideDto ride;
  @Setter
  @ApiModelProperty(required = true)
  private EventType eventType;
  @Setter
  @ApiModelProperty
  private String message;
  @Setter
  @ApiModelProperty
  private String parameters;
  @Setter
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private boolean stacked;

  @JsonProperty
  @ApiModelProperty
  public MobileDriverRideDto getRide() {
    if (stacked) {
      return null;
    }
    return ride;
  }

  @JsonProperty
  @ApiModelProperty
  public MobileDriverRideDto getNextRide() {
    if (stacked) {
      return ride;
    }
    return null;
  }

  public void setRide(MobileDriverRideDto ride, Map<String, Object> parameterObject) {
    Long eta = Optional.ofNullable((Integer) parameterObject.get("eta"))
      .map(Integer::longValue)
      .orElse(null);
    ride.setEstimatedTimeArrive(eta);
    this.ride = ride;
    this.stacked = Optional.ofNullable((Boolean) parameterObject.get("stacked")).orElse(false);
  }
}
