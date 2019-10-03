package com.rideaustin.rest.model;

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ActiveDriverLocationDto {

  private final double lat;
  private final double lng;
  private final Set<String> carCategories;

}
