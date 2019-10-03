package com.rideaustin.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.surgepricing.SurgeFactor;
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;

public class SurgePricingTestUtils {

  public static final String ZIP_CODE = "12345";
  private static final Polygon polygon = Polygon.Builder()
    .addVertex(new Point(-1, 1))
    .addVertex(new Point(-1, -1))
    .addVertex(new Point(1, -1))
    .addVertex(new Point(-11, 1))
    .build();

  private static final String polygonCSV = "-1,1 -1,-1 1,-1 -11,1";

  public static SurgeArea mockSurgeArea(BigDecimal surgeFare) {
    return mockSurgeArea(surgeFare, true);
  }

  public static SurgeArea mockSurgeArea(BigDecimal surgeFare, boolean mockPolygon) {
    SurgeArea area = SurgeArea.builder()
      .cityId(1L)
      .carCategoriesBitmask(1)
      .surgeMapping(ImmutableMap.of("REGULAR", surgeFare))
      .build();
    area.setId(1L);
    area.setSurgeFactors(Sets.newSet(new SurgeFactor(area, "REGULAR", surgeFare)));
    AreaGeometry areaGeometry = new AreaGeometry();
    if (mockPolygon) {
      Polygon polygon = Mockito.mock(Polygon.class);
      when(polygon.contains(any(Point.class))).thenReturn(true);
      areaGeometry.setPolygon(polygon);
    } else {
      areaGeometry.setPolygon(SurgePricingTestUtils.polygon);
    }
    area.setName(ZIP_CODE);
    areaGeometry.setId(1L);
    area.setAreaGeometry(areaGeometry);
    areaGeometry.setCsvGeometry(polygonCSV);
    return area;
  }

}
