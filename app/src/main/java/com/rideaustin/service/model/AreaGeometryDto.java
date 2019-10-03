package com.rideaustin.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AreaGeometryDto {

  private final Double topLeftCornerLat;
  private final Double topLeftCornerLng;
  private final Double bottomRightCornerLat;
  private final Double bottomRightCornerLng;
  private final Double centerPointLat;
  private final Double centerPointLng;
  private final String csvGeometry;
  private final Double labelLat;
  private final Double labelLng;

}
