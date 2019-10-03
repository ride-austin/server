package com.rideaustin.rest.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@RequiredArgsConstructor
public class UrlDto {

  @ApiModelProperty
  private final String url;
}
