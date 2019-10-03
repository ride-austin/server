package com.rideaustin.rest.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalPriorityFareDto {

  @ApiModelProperty(value = "City ID", example = "1", required = true)
  @NotNull(message = "City ID may not be null")
  private Long cityId;
  @ApiModelProperty(value = "Surge mode", allowableValues = "FULL_AUTO,LIMITED_AUTO,MANUAL", required = true)
  @NotNull(message = "Surge mode may not be null")
  private String surgeMode;


}
