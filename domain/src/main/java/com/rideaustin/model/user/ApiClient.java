package com.rideaustin.model.user;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.enums.AvatarType;

@Entity
@Table(name = "api_clients")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiClient extends Avatar {

  public ApiClient() {
    super();
    setActive(true);
  }

  @Override
  public AvatarType getType() {
    return AvatarType.API_CLIENT;
  }

}
