package com.rideaustin.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class CarModelInfo {

  @ApiModelProperty(required = true)
  private final String year;
  @ApiModelProperty(required = true)
  private final String make;
  @ApiModelProperty(required = true)
  private final String model;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public CarModelInfo(@JsonProperty("year") String year, @JsonProperty("make") String make, @JsonProperty("model") String model) {
    this.year = year;
    this.make = make;
    this.model = model;
  }
}
