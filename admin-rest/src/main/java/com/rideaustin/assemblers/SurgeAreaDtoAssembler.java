package com.rideaustin.assemblers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.surgepricing.SurgeFactor;
import com.rideaustin.rest.model.SurgeAreaDto;
import com.rideaustin.service.user.CarTypesUtils;
import com.rideaustin.utils.SurgeUtils;

@Component
public class SurgeAreaDtoAssembler implements BilateralAssembler<SurgeArea, SurgeAreaDto> {

  @Override
  public SurgeArea toDs(SurgeAreaDto dto) {
    Map<String, BigDecimal> surgeFactors = dto.getSurgeFactors();
    Set<SurgeFactor> surgeFactorsCollection = convertSurgeFactors(surgeFactors);
    int carCategoriesBitmask = getCarCategoriesBitmask(surgeFactors);
    SurgeArea surgeArea = SurgeArea.builder()
      .surgeFactors(surgeFactorsCollection)
      .areaGeometry(
        AreaGeometry
          .builder()
          .csvGeometry(dto.getCsvGeometry())
          .bottomRightCornerLat(dto.getBottomRightCornerLat())
          .bottomRightCornerLng(dto.getBottomRightCornerLng())
          .centerPointLat(dto.getCenterPointLat())
          .centerPointLng(dto.getCenterPointLng())
          .labelLat(dto.getLabelLat())
          .labelLng(dto.getLabelLng())
          .topLeftCornerLat(dto.getTopLeftCornerLat())
          .topLeftCornerLng(dto.getTopLeftCornerLng())
          .build()
      )
      .surgeMapping(SurgeUtils.createSurgeMapping(surgeFactorsCollection, carCategoriesBitmask))
      .cityId(dto.getCityId())
      .name(dto.getName())
      .carCategoriesBitmask(carCategoriesBitmask)
      .automated(dto.isAutomated())
      .build();
    surgeArea.setId(dto.getId());
    return surgeArea;
  }

  @Override
  public SurgeAreaDto toDto(SurgeArea area) {
    throw new UnsupportedOperationException("No server -> client DTO interaction");
  }

  private int getCarCategoriesBitmask(Map<String, BigDecimal> surgeFactors) {
    return CarTypesUtils.toBitMask(surgeFactors.keySet());
  }

  private Set<SurgeFactor> convertSurgeFactors(Map<String, BigDecimal> surgeFactors) {
    return surgeFactors.entrySet()
      .stream()
      .map(e -> new SurgeFactor(null, e.getKey(), e.getValue()))
      .collect(Collectors.toSet());
  }
}
