package com.rideaustin.test.fixtures;

import com.rideaustin.model.surgepricing.AreaGeometry;

public class AreaGeometryFixture extends AbstractFixture<AreaGeometry> {

  @Override
  protected AreaGeometry createObject() {
    return AreaGeometry.builder()
      .centerPointLat(30.2815919)
      .centerPointLng(-97.7320897)
      .topLeftCornerLat(-97.7407)
      .topLeftCornerLng(30.2905)
      .bottomRightCornerLat(-97.7232)
      .bottomRightCornerLng(30.2716)
      .csvGeometry("-97.74070000000002,30.27380000000001 -97.73759999999999,30.2817 -97.7367,30.2905 -97.73010000000001,30.2898 -97.724,30.2873 -97.724,30.2835 -97.72319999999999,30.2794 -97.7289,30.27840000000001 -97.7302,30.2762 -97.7319,30.2716 -97.74070000000002,30.27380000000001")
      .labelLat(30.2815919)
      .labelLng(-97.7320897)
      .build();
  }
}
