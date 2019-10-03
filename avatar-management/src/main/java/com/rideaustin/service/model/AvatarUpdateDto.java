package com.rideaustin.service.model;

import com.rideaustin.model.user.Avatar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AvatarUpdateDto {

  private final String fullName;
  private final String email;
  private final long cityId;
  private final boolean active;

  public AvatarUpdateDto(Avatar avatar) {
    this(avatar.getFullName(), avatar.getEmail(), avatar.getCityId(), avatar.isActive());
  }
}
