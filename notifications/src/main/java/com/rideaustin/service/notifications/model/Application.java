package com.rideaustin.service.notifications.model;

import com.rideaustin.service.notifications.ApplicationPolicy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Application {

  private Long id;
  private String arn;
  private String applicationPolicyClassName;

  private ApplicationPolicy policy;

}
