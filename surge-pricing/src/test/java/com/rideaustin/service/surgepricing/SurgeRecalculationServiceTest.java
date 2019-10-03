package com.rideaustin.service.surgepricing;

import static com.rideaustin.service.surgepricing.SurgeRecalculationConfig.range;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.application.cache.RefreshCacheException;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.clients.configuration.ConfigurationItemService;
import com.rideaustin.model.redis.AreaGeometry;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.repo.redis.SurgeAreaRedisRepository;
import com.rideaustin.service.config.SurgeMode;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class SurgeRecalculationServiceTest {

  private static final double THRESHOLD = 0.6;
  private static final double MAX_AUTHORIZED_VALUE = 3.0;

  @Mock
  private SurgePricingStatsAggregator statsAggregator;
  @Mock
  private SurgeAreaRedisRepository surgeAreaRedisRepository;
  @Mock
  private SurgeAreaDslRepository surgeAreaDslRepository;
  @Mock
  private ConfigurationItemCache configurationCache;
  @Mock
  private ConfigurationItemService configurationService;
  @Mock
  private SurgeAreaHistoryService historyService;
  @Mock
  private SurgePricingNotificationService notificationService;
  @Mock
  private SurgeRecalculationConfigProvider configProvider;
  @Mock
  private ObjectMapper objectMapper;
  @Captor
  private ArgumentCaptor<List<SurgeArea>> savedAreasCaptor;

  private SurgeRecalculationService testedInstance;

  private final static Long CITY_ID = 1L;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    CarTypesUtils.setCarTypesCache(mock(CarTypesCache.class));
    testedInstance = new SurgeRecalculationServiceImpl(configurationCache, configurationService,
      objectMapper, statsAggregator, notificationService,
      new TimeService(), surgeAreaDslRepository, surgeAreaRedisRepository,
      historyService, configProvider);
  }

  @DataProvider
  public static Object[] limitedAutoModeFixture() {
    return new Object[] {
      ImmutablePair.of(28, 1.0),
      ImmutablePair.of(109, 1.0),
      ImmutablePair.of(115, 1.25),
      ImmutablePair.of(155, 1.25),
      ImmutablePair.of(160, 1.5),
      ImmutablePair.of(168, 1.5),
      ImmutablePair.of(170, 1.75),
      ImmutablePair.of(215, 1.75),
      ImmutablePair.of(219, 2.0),
      ImmutablePair.of(240, 2.0),
      ImmutablePair.of(241, 2.25),
      ImmutablePair.of(260, 2.25),
      ImmutablePair.of(265, 2.5),
      ImmutablePair.of(280, 2.5),
      ImmutablePair.of(285, 2.75),
      ImmutablePair.of(298, 2.75),
      ImmutablePair.of(300, 3.0),
      ImmutablePair.of(333, 3.0),
      ImmutablePair.of(335, 3.0),
      ImmutablePair.of(365, 3.0),
      ImmutablePair.of(369, 3.0),
      ImmutablePair.of(396, 3.0),
      ImmutablePair.of(400, 3.0),
      ImmutablePair.of(425, 3.0),
      ImmutablePair.of(500, 3.0)
    };
  }

  @DataProvider
  public static Object[] fullAutoModeFixture() {
    return new Object[] {
      ImmutablePair.of(28, 1.0),
      ImmutablePair.of(109, 1.0),
      ImmutablePair.of(115, 1.25),
      ImmutablePair.of(155, 1.25),
      ImmutablePair.of(160, 1.5),
      ImmutablePair.of(168, 1.5),
      ImmutablePair.of(170, 1.75),
      ImmutablePair.of(215, 1.75),
      ImmutablePair.of(219, 2.0),
      ImmutablePair.of(240, 2.0),
      ImmutablePair.of(241, 2.25),
      ImmutablePair.of(260, 2.25),
      ImmutablePair.of(265, 2.5),
      ImmutablePair.of(280, 2.5),
      ImmutablePair.of(285, 2.75),
      ImmutablePair.of(298, 2.75),
      ImmutablePair.of(300, 3.0),
      ImmutablePair.of(333, 3.0),
      ImmutablePair.of(335, 3.5),
      ImmutablePair.of(365, 3.5),
      ImmutablePair.of(369, 4.0),
      ImmutablePair.of(396, 4.0),
      ImmutablePair.of(400, 4.5),
      ImmutablePair.of(425, 4.5),
      ImmutablePair.of(500, 5.0)
    };
  }

  @Test
  public void testCalculationSkippedInManualMode() {
    setupConfig(SurgeMode.MANUAL);

    testedInstance.recalculateSurgePricingAreas(CITY_ID);

    verify(surgeAreaDslRepository, never()).saveMany(anyListOf(SurgeArea.class));
  }

  @Test
  public void testCalculationSkippedIfNoCarsInArea() {
    setupConfig(SurgeMode.LIMITED_AUTO);
    setupCalculationTest(0, 0, 0);

    testedInstance.recalculateSurgePricingAreas(CITY_ID);

    assertCalculationsSkipped();
  }

  @Test
  public void testCalculationSkippedIfUtilizationRateIsLowerThanThreshold() {
    setupConfig(SurgeMode.LIMITED_AUTO);
    setupCalculationTest(100, 50, 0);

    testedInstance.recalculateSurgePricingAreas(CITY_ID);

    assertCalculationsSkipped();
  }

  @Test
  @UseDataProvider("limitedAutoModeFixture")
  public void testCalculationInLimitedAutoMode(Pair<Integer, Double> data) {
    setupConfig(SurgeMode.LIMITED_AUTO);
    setupCalculationTest(100, 30, data.getKey());

    testedInstance.recalculateSurgePricingAreas(CITY_ID);

    assertCalculationResult(data.getValue());
  }

  @Test
  public void testUpdateConfigRefreshesCache() throws Exception {
    when(configurationService.update(any(), any())).thenReturn(true);

    boolean result = testedInstance.updateConfig(1L, new SurgeRecalculationConfig());

    verify(configurationCache, times(1)).refreshCache();
    assertTrue(result);
  }

  @Test
  public void testUpdateConfigReturnsFalseOnConfigRefreshFail() throws Exception {
    when(configurationService.update(any(), any())).thenReturn(true);
    doThrow(new RefreshCacheException()).when(configurationCache).refreshCache();

    boolean result = testedInstance.updateConfig(1L, new SurgeRecalculationConfig());

    assertFalse(result);
  }

  @Test
  @UseDataProvider("fullAutoModeFixture")
  public void testCalculationInFullAutoMode(Pair<Integer, Double> data) {
    setupConfig(SurgeMode.FULL_AUTO);
    setupCalculationTest(100, 30, data.getKey());

    testedInstance.recalculateSurgePricingAreas(CITY_ID);

    assertCalculationResult(data.getValue());
  }

  private void setupCalculationTest(int numberOfCars, int numberOfAvailableCars, int numberOfEyeballs) {
    RedisSurgeArea area = new RedisSurgeArea();
    area.setId(1L);
    area.setAreaGeometry(new AreaGeometry());
    area.setAutomated(true);
    area.setNumberOfCars(ImmutableMap.of("REGULAR", numberOfCars));
    area.setNumberOfAvailableCars(ImmutableMap.of("REGULAR", numberOfAvailableCars));
    area.setCarCategoriesNumberOfEyeballs(ImmutableMap.of("REGULAR", numberOfEyeballs));

    Map<String, BigDecimal> surgeMapping = new HashMap<>();
    surgeMapping.put("REGULAR", BigDecimal.ONE);
    area.setSurgeMapping(surgeMapping);

    Map<String, BigDecimal> recommendedSurgeMapping = new HashMap<>();
    recommendedSurgeMapping.put("REGULAR", BigDecimal.ONE);
    area.setRecommendedSurgeMapping(recommendedSurgeMapping);

    when(statsAggregator.updateSurgeAreasStats(any(Date.class), any(Date.class), eq(CITY_ID))).thenReturn(Collections.singletonList(area));

    SurgeArea surgeArea = new SurgeArea();
    surgeArea.setId(1L);
    surgeArea.setAutomated(true);
    surgeArea.setCarCategoriesBitmask(1);
    surgeArea.setAreaGeometry(new com.rideaustin.model.surgepricing.AreaGeometry());
    when(surgeAreaDslRepository.findAllAutomated(anySetOf(Long.class))).thenReturn(Collections.singletonList(surgeArea));
  }

  private void assertCalculationResult(double expected) {
    verify(surgeAreaDslRepository, times(1)).saveMany(savedAreasCaptor.capture());
    assertThat(savedAreasCaptor.getValue().isEmpty(), is(false));
    assertThat(savedAreasCaptor.getValue().get(0).getSurgeFactors().iterator().next().getValue(), is(closeTo(BigDecimal.valueOf(expected), BigDecimal.ZERO)));
  }

  private void assertCalculationsSkipped() {
    verify(surgeAreaDslRepository, times(1)).saveMany(savedAreasCaptor.capture());
    assertThat(savedAreasCaptor.getValue().isEmpty(), is(false));
    assertThat(savedAreasCaptor.getValue().get(0).getSurgeFactors().iterator().next().getValue(), is(closeTo(BigDecimal.ONE, BigDecimal.ZERO)));
  }

  private void setupConfig(SurgeMode mode) {
    Map<String, Object> config = ImmutableMap.<String, Object>builder()
      .put(SurgeRecalculationConfig.SURGE_EQUATION_MAPPING_KEY, ImmutableMap.<String, BigDecimal>builder()
        .put(range(-1000, 99), BigDecimal.ONE)
        .put(range(100, 174), BigDecimal.valueOf(1.25))
        .put(range(175, 199), BigDecimal.valueOf(1.5))
        .put(range(200, 299), BigDecimal.valueOf(1.75))
        .put(range(300, 349), BigDecimal.valueOf(2.0))
        .put(range(350, 399), BigDecimal.valueOf(2.25))
        .put(range(400, 449), BigDecimal.valueOf(2.5))
        .put(range(450, 499), BigDecimal.valueOf(2.75))
        .put(range(500, 599), BigDecimal.valueOf(3.0))
        .put(range(600, 699), BigDecimal.valueOf(3.5))
        .put(range(700, 799), BigDecimal.valueOf(4.0))
        .put(range(800, 899), BigDecimal.valueOf(4.5))
        .put(range(900, Integer.MAX_VALUE), BigDecimal.valueOf(5.0))
        .build())
      .put(SurgeRecalculationConfig.SURGE_MODE_KEY, mode.name())
      .put(SurgeRecalculationConfig.UTILIZATION_THRESHOLD_KEY, THRESHOLD)
      .put(SurgeRecalculationConfig.DEFAULT_AREA_MONITORING_PERIOD_KEY, 1800000)
      .put(SurgeRecalculationConfig.MAX_AUTHORIZED_LIMITED_AUTO_VALUE_KEY, MAX_AUTHORIZED_VALUE)
      .build();
    when(configProvider.getConfig(anyLong())).thenReturn(new SurgeRecalculationConfig(config));
  }

}