package com.rideaustin.model.user;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.enums.AvatarType;

import lombok.Builder;

@Entity
@Table(name = "administrators")
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class Administrator extends Avatar {

  public Administrator() {
    super();
    setActive(true);
  }

  @Override
  public AvatarType getType() {
    return AvatarType.ADMIN;
  }

}
