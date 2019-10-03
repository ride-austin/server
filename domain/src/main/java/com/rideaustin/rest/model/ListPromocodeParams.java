package com.rideaustin.rest.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ListPromocodeParams {

  @ApiModelProperty
  private String codeLiteral;

  @ApiModelProperty(example = "1")
  private Integer cityBitMask;

}
