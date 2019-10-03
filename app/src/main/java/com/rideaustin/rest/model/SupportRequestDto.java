package com.rideaustin.rest.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class SupportRequestDto {

  @ApiModelProperty(value = "Support topic ID", required = true)
  private Long topicId;
  @ApiModelProperty("Ride ID")
  private Long rideId;
  @ApiModelProperty("Support request comments")
  private String comments;
}
