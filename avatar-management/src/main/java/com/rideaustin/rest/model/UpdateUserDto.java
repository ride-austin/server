package com.rideaustin.rest.model;

import com.rideaustin.model.user.Gender;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class UpdateUserDto {
  @ApiModelProperty
  private String firstname;
  @ApiModelProperty
  private String lastname;
  @ApiModelProperty
  private String nickName;
  @ApiModelProperty
  private Gender gender;
  @ApiModelProperty
  private String phoneNumber;
}
