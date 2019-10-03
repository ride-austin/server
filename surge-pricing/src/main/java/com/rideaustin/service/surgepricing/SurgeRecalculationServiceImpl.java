package com.rideaustin.service.surgepricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.Constants;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.clients.configuration.ConfigurationItemService;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.repo.redis.SurgeAreaRedisRepository;
import com.rideaustin.service.config.SurgeMode;
import com.rideaustin.service.generic.TimeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SurgeRecalculationServiceImpl extends AbstractSurgeRecalculationService {

  private final SurgePricingStatsAggregator statsAggregator;
  private final TimeService timeService;
  private final SurgeRecalculationConfigProvider configProvider;

  @Inject
  public SurgeRecalculationServiceImpl(ConfigurationItemCache configurationCache, ConfigurationItemService configurationService,
                                       ObjectMapper objectMapper, SurgePricingStatsAggregator statsAggregator, SurgePricingNotificationService notificationService,
                                       TimeService timeService, SurgeAreaDslRepository surgeAreaDslRepository, SurgeAreaRedisRepository surgeAreaRedisRepository,
                                       SurgeAreaHistoryService historyService, SurgeRecalculationConfigProvider configProvider) {
    super(historyService, configurationCache, configurationService, objectMapper, notificationService, surgeAreaDslRepository, surgeAreaRedisRepository);
    this.statsAggregator = statsAggregator;
    this.timeService = timeService;
    this.configProvider = configProvider;
  }

  /**
   * For each surge area in each city we update current surge area stats {@link SurgePricingStatsAggregator#updateSurgeAreasStats}
   * After that we recalculate recommended surge factors according to configuration {@link SurgeRecalculationServiceImpl#calculateRecommendedSurgeFactors}
   * If automated priority fare is not disabled in config, we remove previous surge factor values and set new ones
   */
  @Override
  @Transactional
  public void recalculateSurgePricingAreas(Long city) {

    Date currentDate = timeService.getCurrentDate();

    SurgeRecalculationConfig config = configProvider.getConfig(city);
    Date startDate = DateUtils.addMilliseconds(currentDate, -config.getDefaultAreaMonitoringPeriod());
    List<RedisSurgeArea> surgeAreas = statsAggregator.updateSurgeAreasStats(currentDate, startDate, city);
    for (RedisSurgeArea surgeArea : surgeAreas) {
      calculateRecommendedSurgeFactors(config, surgeArea);
      if (config.getSurgeMode() != SurgeMode.MANUAL) {
        historyService.persistHistoryItem(surgeArea);
      }
    }
    persistCalculationResults(config, surgeAreas);
  }

  /**
   * Recalculate surge factors according to configuration stored in db
   * First we check if calculations need to be done - we skip it if either of the following is true:
   * * automated priority fare is turned off
   * * there's no cars in area
   * * utilization rate (1 - avail. cars / total cars) is less than threshold defined in config
   * * Then we calculate <i>pfValue</i> which is
   * * X = Eyes/Avail Cars
   * * Y = Eyes - Avail Cars
   * * Equation is 3x^2+.75y
   * We search for one of the ranges in configuration which includes calculated pfValue and set recommended surge factor
   * to the value that is assigned to this range
   */
  private void calculateRecommendedSurgeFactors(SurgeRecalculationConfig config, RedisSurgeArea surgeArea) {
    for (Map.Entry<String, Integer> entry : surgeArea.getNumberOfAvailableCars().entrySet()) {

      String carCategory = entry.getKey();
      Integer availableCars = entry.getValue();

      if (shouldSkipCalculations(config, surgeArea, carCategory, availableCars)) {
        surgeArea.getRecommendedSurgeMapping().put(carCategory, Constants.NEUTRAL_SURGE_FACTOR);
        if (surgeArea.isAutomated() && config.getSurgeMode() != SurgeMode.MANUAL) {
          surgeArea.getSurgeMapping().put(carCategory, Constants.NEUTRAL_SURGE_FACTOR);
        }
        continue;
      }

      BigDecimal divisor = BigDecimal.valueOf(Optional.ofNullable(availableCars).orElse(1));
      if (availableCars == 0) {
        divisor = BigDecimal.ONE;
      }
      BigDecimal x = BigDecimal.valueOf(surgeArea.getCarCategoriesNumberOfEyeballs().get(carCategory)).divide(divisor, 3, BigDecimal.ROUND_CEILING);
      BigDecimal y = BigDecimal.valueOf(surgeArea.getCarCategoriesNumberOfEyeballs().get(carCategory)).subtract(BigDecimal.valueOf(Optional.ofNullable(availableCars).orElse(0)));
      BigDecimal pfValue = x.pow(2).multiply(BigDecimal.valueOf(3d)).add(y.multiply(BigDecimal.valueOf(0.75))).setScale(0, RoundingMode.FLOOR);

      for (Map.Entry<Range<BigDecimal>, BigDecimal> mappingEntry : config.getSurgeEquationMapping().entrySet()) {
        if (mappingEntry.getKey().contains(pfValue)) {
          BigDecimal recommended = mappingEntry.getValue();
          if (config.getSurgeMode() == SurgeMode.LIMITED_AUTO) {
            recommended = recommended.min(config.getMaxAuthorizedLimitedAutoValue());
          }
          surgeArea.getRecommendedSurgeMapping().put(carCategory, recommended);
          break;
        }
      }
    }
  }

  private boolean shouldSkipCalculations(SurgeRecalculationConfig config, RedisSurgeArea surgeArea, String carCategory, Integer availableCars) {
    Integer totalCars = surgeArea.getNumberOfCars().getOrDefault(carCategory, 0);
    if (totalCars == 0) {
      return true;
    }
    BigDecimal utilizationRate = BigDecimal.ONE.subtract(BigDecimal.valueOf(availableCars).divide(BigDecimal.valueOf(totalCars), 2, RoundingMode.HALF_EVEN));
    return utilizationRate.compareTo(config.getUtilizationThreshold()) < 0;
  }

}
