package com.rideaustin.service.notifications.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rideaustin.service.notifications.SubscriptionPolicy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class Topic {

  @ApiModelProperty(required = true)
  private Long id;
  @ApiModelProperty(required = true)
  private String name;
  @ApiModelProperty
  private String description;
  @ApiModelProperty(required = true)
  private String arn;
  @ApiModelProperty(required = true)
  private String subscriptionPolicyClassName;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private SubscriptionPolicy policy;

}
