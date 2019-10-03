package com.rideaustin.rest.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class DriverEmailReminderParams {
  @ApiModelProperty("Reminder subject")
  private String subject;
  @ApiModelProperty("Reminder content")
  private String content;

}
