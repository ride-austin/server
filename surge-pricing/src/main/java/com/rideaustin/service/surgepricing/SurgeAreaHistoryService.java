package com.rideaustin.service.surgepricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.Constants;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.surgepricing.SurgeAreaHistory;
import com.rideaustin.model.surgepricing.SurgeFactor;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.repo.redis.SurgeAreaRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SurgeAreaHistoryService {

  private final SurgeAreaDslRepository surgeAreaDslRepository;
  private final SurgeAreaRedisRepository surgeAreaRedisRepository;

  public void persistHistoryItem(RedisSurgeArea redisSurgeArea) {
    SurgeArea surgeArea = surgeAreaDslRepository.findOne(redisSurgeArea.getId());
    if (surgeArea == null) {
      return;
    }
    save(redisSurgeArea, surgeArea);
  }

  public void persistHistoryItem(SurgeArea surgeArea) {
    RedisSurgeArea redisSurgeArea = surgeAreaRedisRepository.findOne(surgeArea.getId());
    save(redisSurgeArea, surgeArea);
  }

  private void save(RedisSurgeArea redisSurgeArea, SurgeArea surgeArea) {
    List<SurgeAreaHistory> surgeAreaHistoryItems = createSurgeAreaHistoryItem(surgeArea, redisSurgeArea);
    for (SurgeAreaHistory surgeAreaHistoryItem : surgeAreaHistoryItems) {
      SurgeAreaHistory previousHistoryItem = surgeAreaDslRepository.findLatestHistoryItem(surgeArea, surgeAreaHistoryItem.getSurgeFactorCarCategory());
      if (previousHistoryItem == null || shouldStoreHistoryItem(previousHistoryItem, surgeAreaHistoryItem)) {
        surgeAreaDslRepository.save(surgeAreaHistoryItem);
      }
    }
  }

  private List<SurgeAreaHistory> createSurgeAreaHistoryItem(SurgeArea surgeArea, RedisSurgeArea redisSurgeArea) {
    AreaGeometry areaGeometry = surgeArea.getAreaGeometry();

    List<SurgeAreaHistory> historyItems = new ArrayList<>();
    Map<String, BigDecimal> recommendedSurgeFactors = redisSurgeArea.getRecommendedSurgeMapping();
    for (SurgeFactor surgeFactor : surgeArea.getSurgeFactors()) {
      historyItems.add(
        SurgeAreaHistory.builder()
          .surgeFactor(surgeFactor.getValue())
          .surgeFactorCarCategory(surgeFactor.getCarType())
          .carCategoriesBitmask(surgeArea.getCarCategoriesBitmask())
          .recommendedSurgeFactor(recommendedSurgeFactors.getOrDefault(surgeFactor.getCarType(), Constants.NEUTRAL_SURGE_FACTOR))
          .numberOfCars(redisSurgeArea.getNumberOfCars().getOrDefault(surgeFactor.getCarType(), 0))
          .numberOfRequestedRides(redisSurgeArea.getNumberOfRequestedRides().getOrDefault(surgeFactor.getCarType(), 0))
          .numberOfAcceptedRides(redisSurgeArea.getNumberOfAcceptedRides().getOrDefault(surgeFactor.getCarType(), 0))
          .numberOfEyeballs(redisSurgeArea.getCarCategoriesNumberOfEyeballs().getOrDefault(surgeFactor.getCarType(), 0))
          .numberOfCars(redisSurgeArea.getNumberOfCars().getOrDefault(surgeFactor.getCarType(), 0))
          .numberOfAvailableCars(redisSurgeArea.getNumberOfAvailableCars().getOrDefault(surgeFactor.getCarType(), 0))
          .name(surgeArea.getName())
          .surgeAreaId(surgeArea.getId())
          .topLeftCornerLat(areaGeometry.getTopLeftCornerLat())
          .topLeftCornerLng(areaGeometry.getTopLeftCornerLng())
          .bottomRightCornerLat(areaGeometry.getBottomRightCornerLat())
          .bottomRightCornerLng(areaGeometry.getBottomRightCornerLng())
          .centerPointLat(areaGeometry.getCenterPointLat())
          .centerPointLng(areaGeometry.getCenterPointLng())
          .csvGeometry(areaGeometry.getCsvGeometry())
          .build()
      );
    }
    return historyItems;
  }

  private boolean shouldStoreHistoryItem(SurgeAreaHistory existing, SurgeAreaHistory updatedSurgeArea) {
    return !existing.equalsTo(updatedSurgeArea);
  }
}
