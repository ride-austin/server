package com.rideaustin.service.surgepricing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.clients.configuration.ConfigurationItemService;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.repo.redis.SurgeAreaRedisRepository;
import com.rideaustin.service.config.SurgeMode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public abstract class AbstractSurgeRecalculationService implements SurgeRecalculationService {

  protected final SurgeAreaHistoryService historyService;
  private final ConfigurationItemCache configurationCache;
  private final ConfigurationItemService configurationService;
  private final ObjectMapper objectMapper;
  private final SurgePricingNotificationService notificationService;
  private final SurgeAreaDslRepository surgeAreaDslRepository;
  protected final SurgeAreaRedisRepository surgeAreaRedisRepository;

  @Override
  @Transactional
  public boolean updateConfig(Long cityId, SurgeRecalculationConfig config) {
    ConfigurationItem item = configurationService.findByKeyAndCityId(Config.SURGE_CONFIG_KEY, cityId);
    boolean result = configurationService.update(item, config.asMap());
    if (result) {
      try {
        configurationCache.refreshCache();
      } catch (Exception e) {
        log.error("Failed to update surge area config", e);
        result = false;
      }
    }
    return result;
  }

  @Override
  @Transactional
  public boolean updatePriorityFareMode(Long cityId, SurgeMode surgeMode) {
    ConfigurationItem item = configurationService.findByKeyAndCityId(Config.SURGE_CONFIG_KEY, cityId);
    if (item != null) {
      SurgeRecalculationConfig config = objectMapper.convertValue(item.getConfigurationValue(), SurgeRecalculationConfig.class);
      config.setSurgeMode(surgeMode);
      return updateConfig(cityId, config);
    }
    return false;
  }

  protected void persistCalculationResults(SurgeRecalculationConfig config, List<RedisSurgeArea> surgeAreas) {
    surgeAreaRedisRepository.save(surgeAreas);

    if (config.getSurgeMode() != SurgeMode.MANUAL) {
      Map<Long, Map<String, BigDecimal>> recommendedMapping = surgeAreas.stream().collect(Collectors.toMap(
        RedisSurgeArea::getId,
        RedisSurgeArea::getRecommendedSurgeMapping
      ));
      Map<Long, RedisSurgeArea> recommendedAreas = surgeAreas.stream().collect(Collectors.toMap(
        RedisSurgeArea::getId,
        Function.identity()
      ));
      List<SurgeArea> automatedAreas = surgeAreaDslRepository.findAllAutomated(recommendedMapping.keySet());
      for (SurgeArea area : automatedAreas) {
        area.updateSurgeFactors(recommendedMapping.get(area.getId()));
        recommendedAreas.get(area.getId()).updateFieldsFrom(area);
      }
      surgeAreaDslRepository.saveMany(automatedAreas);
      notificationService.notifyUsers(automatedAreas);
      surgeAreaRedisRepository.save(recommendedAreas.values());
    }

  }
}
