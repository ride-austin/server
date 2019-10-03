package com.rideaustin.rest.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class AuthenticationToken {

  @ApiModelProperty("Authentication token to be used as X-Auth-Token header content")
  private String token;

}
