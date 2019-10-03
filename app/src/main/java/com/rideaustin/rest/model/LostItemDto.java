package com.rideaustin.rest.model;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class LostItemDto {

  @NotEmpty
  @Length(max = 2000)
  @ApiModelProperty(value = "Lost item description", required = true)
  private String description;

  @NotEmpty
  @Length(max = 2000)
  @ApiModelProperty(value = "Extra details about the lost item", required = true)
  private String details;

}
