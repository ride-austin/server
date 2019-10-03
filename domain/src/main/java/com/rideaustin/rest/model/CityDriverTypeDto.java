package com.rideaustin.rest.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.rideaustin.model.ride.CityDriverType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityDriverTypeDto {

  @ApiModelProperty(required = true)
  private final Long cityId;
  @ApiModelProperty(required = true)
  private final Set<String> availableInCategories;
  private final String name;
  @ApiModelProperty(required = true)
  private final String description;
  @JsonUnwrapped
  private final CityDriverType.Configuration configuration;

  /**
   * Do not remove - constructor is used in ITests
   */
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public CityDriverTypeDto(@JsonProperty("cityId") Long cityId, @JsonProperty("availableInCategories") Set<String> availableInCategories,
    @JsonProperty("name") String name, @JsonProperty("description") String description) {
    this.cityId = cityId;
    this.availableInCategories = availableInCategories;
    this.name = name;
    this.description = description;
    this.configuration = null;
  }

}