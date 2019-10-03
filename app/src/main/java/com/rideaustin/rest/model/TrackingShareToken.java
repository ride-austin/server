package com.rideaustin.rest.model;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ApiModel
@RequiredArgsConstructor
public class TrackingShareToken {

  private final String token;

}
