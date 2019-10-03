package com.rideaustin.service.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
public class DirectConnectDriverDto {

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final long id;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final long driverId;
  @ApiModelProperty(required = true)
  private final String firstName;
  @ApiModelProperty(required = true)
  private final String lastName;
  @Setter
  @ApiModelProperty
  private String photoUrl;
  @ApiModelProperty(required = true)
  private final double rating;
  @Setter
  @ApiModelProperty("Available car categories")
  private Set<String> categories;
  @ApiModelProperty("Surge factors per car category")
  private final Map<String, BigDecimal> factors;

  @QueryProjection
  public DirectConnectDriverDto(long id, long driverId, String firstName, String lastName, double rating) {
    this.id = id;
    this.driverId = driverId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.rating = rating;
    this.factors = new HashMap<>();
  }
}
