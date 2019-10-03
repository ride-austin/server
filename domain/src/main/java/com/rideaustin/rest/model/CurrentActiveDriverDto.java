package com.rideaustin.rest.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.ActiveDriverStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
public class CurrentActiveDriverDto extends CompactActiveDriverDto {

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final int availableCarCategories;
  @Setter
  @ApiModelProperty(required = true)
  private Set<String> carCategories;

  @QueryProjection
  public CurrentActiveDriverDto(long id, long driverId, long userId, ActiveDriverStatus status, int availableCarCategories) {
    super(id, driverId, userId, status);
    this.availableCarCategories = availableCarCategories;
  }
}
