package com.rideaustin.service.surgepricing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.surgepricing.SurgeAreaHistory;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.repo.redis.SurgeAreaRedisRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ConflictException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.SurgePricingTestUtils;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class SurgePricingServiceTest {

  private static final long SURGE_AREA_ID = 123L;
  private static final String EXISTING_NAME = "Name";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private SurgeAreaDslRepository surgeAreaDslRepository;
  @Mock
  private SurgePricingNotificationService notificationService;
  @Mock
  private SurgeAreaRedisRepository surgeAreaRedisRepository;
  @Mock
  private SurgeAreaHistoryService surgeAreaHistoryService;

  private SurgePricingService surgePricingService;
  private static final Map<Integer, String> CAR_TYPE_MAPPING = ImmutableMap.of(
    1, "REGULAR",
    2, "SUV",
    4, "PREMIUM",
    16, "LUXURY"
  );

  @DataProvider
  public static Object[] surgeAreasForMaximumFactor() {
    RedisSurgeArea sa1 = mockSurgeArea(ImmutableSet.of("REGULAR", "PREMIUM"), 5, 5);
    RedisSurgeArea sa2 = mockSurgeArea(ImmutableSet.of("PREMIUM", "SUV"), 6, 8);
    RedisSurgeArea sa3 = mockSurgeArea(ImmutableSet.of("SUV", "PREMIUM"), 5, 6);
    RedisSurgeArea sa4 = mockSurgeArea(ImmutableSet.of("REGULAR", "PREMIUM", "SUV"), 7, 3);
    RedisSurgeArea sa5 = mockSurgeArea(ImmutableSet.of("PREMIUM"), 4, 7);
    RedisSurgeArea sa6 = mockSurgeArea(ImmutableSet.of("SUV"), 2, 12);
    RedisSurgeArea sa7 = mockSurgeArea(ImmutableSet.of("REGULAR"), 1, 6);
    RedisSurgeArea sa8 = mockSurgeArea(ImmutableSet.of("SUV", "PREMIUM"), 5, 5);
    return new Object[]{
      ImmutableTriple.of(ImmutableList.of(sa1, sa2, sa3), 2, BigDecimal.valueOf(8.0)),
      ImmutableTriple.of(ImmutableList.of(sa1, sa2, sa3, sa6), 2, BigDecimal.valueOf(12.0)),
      ImmutableTriple.of(ImmutableList.of(sa1, sa3, sa5, sa8), 4, BigDecimal.valueOf(7.0)),
      ImmutableTriple.of(ImmutableList.of(sa4, sa5, sa7, sa8), 1, BigDecimal.valueOf(6.0))
    };
  }

  @Before
  public void setupTests() throws ParseException {
    MockitoAnnotations.initMocks(this);

    SurgeArea surgeArea = SurgePricingTestUtils.mockSurgeArea(BigDecimal.valueOf(1.5d));

    when(surgeAreaDslRepository.findByAreaGeometries(anyListOf(AreaGeometry.class))).thenReturn(Lists.newArrayList(surgeArea));
    when(surgeAreaDslRepository.save(any(SurgeArea.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
    when(surgeAreaDslRepository.save(any(SurgeAreaHistory.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

    surgePricingService = new SurgePricingService(surgeAreaDslRepository, surgeAreaRedisRepository, surgeAreaHistoryService, notificationService);
  }

  @Test(expected = NotFoundException.class)
  public void testUpdateSurgeAreaThrowsNFEOnNonExistentArea() throws RideAustinException {
    SurgeArea surgeArea = SurgePricingTestUtils.mockSurgeArea(BigDecimal.ONE);

    when(surgeAreaDslRepository.findOne(anyLong())).thenReturn(null);

    surgePricingService.updateSurgeArea(1L, surgeArea, true);

    verify(surgeAreaDslRepository, times(1)).findOne(SURGE_AREA_ID);
  }

  @Test
  public void testUpdateSurgeAreaThrowsNFEOnIncorrectPolygon() throws RideAustinException {
    SurgeArea surgeArea = SurgePricingTestUtils.mockSurgeArea(BigDecimal.ONE);
    surgeArea.getAreaGeometry().setCsvGeometry("-1,1");
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Polygon must have at least 3 points");

    surgePricingService.updateSurgeArea(1L, surgeArea, true);

    verify(surgeAreaDslRepository, times(1)).findOne(SURGE_AREA_ID);
  }

  @Test
  public void testUpdateSurgeAreaCorrectValue() throws RideAustinException {
    SurgeArea surgeArea = SurgePricingTestUtils.mockSurgeArea(BigDecimal.ONE);
    SurgeArea existing = SurgePricingTestUtils.mockSurgeArea(BigDecimal.ONE);
    existing.setId(SURGE_AREA_ID);
    when(surgeAreaDslRepository.findOne(anyLong())).thenReturn(existing);
    when(surgeAreaRedisRepository.findOne(anyLong())).thenReturn(new RedisSurgeArea());

    surgePricingService.updateSurgeArea(SURGE_AREA_ID, surgeArea, true);

    verify(surgeAreaDslRepository, times(1)).findOne(anyLong());
  }

  @Test
  public void testUpdateSurgeAreaCorrectValueHistoryUpdate() throws RideAustinException {
    SurgeArea surgeArea = SurgePricingTestUtils.mockSurgeArea(BigDecimal.ONE);
    SurgeArea existing = SurgePricingTestUtils.mockSurgeArea(BigDecimal.valueOf(2.0));
    existing.setId(1L);
    when(surgeAreaDslRepository.findOne(anyLong())).thenReturn(existing);
    when(surgeAreaRedisRepository.findOne(anyLong())).thenReturn(new RedisSurgeArea());

    surgePricingService.updateSurgeArea(1L, surgeArea, true);

    ArgumentCaptor<BaseEntity> argument = ArgumentCaptor.forClass(BaseEntity.class);
    verify(surgeAreaDslRepository, times(1)).save(argument.capture());

    List<BaseEntity> surgeAreaArg = argument.getAllValues();

    assertThat(((SurgeArea) surgeAreaArg.get(0)).getSurgeFactors().iterator().next().getValue(), is(closeTo(BigDecimal.ONE, BigDecimal.ZERO)));
  }

  @Test
  public void testUpdateSurgeAreaSendNotification() throws RideAustinException {
    CarTypesCache carTypesCache = mock(CarTypesCache.class);
    CarTypesUtils.setCarTypesCache(carTypesCache);

    SurgeArea surgeArea = SurgePricingTestUtils.mockSurgeArea(BigDecimal.valueOf(2.0d), false);
    SurgeArea existing = SurgePricingTestUtils.mockSurgeArea(BigDecimal.ONE, false);

    existing.setId(SURGE_AREA_ID);
    when(surgeAreaDslRepository.findOne(anyLong())).thenReturn(existing);
    when(surgeAreaRedisRepository.findOne(anyLong())).thenReturn(new RedisSurgeArea());

    surgePricingService.updateSurgeArea(SURGE_AREA_ID, surgeArea, true);

    ArgumentCaptor<SurgeArea> argument = ArgumentCaptor.forClass(SurgeArea.class);
    verify(surgeAreaDslRepository, times(1)).save(argument.capture());

    List<SurgeArea> surgeAreaArg = argument.getAllValues();

    assertThat(surgeAreaArg.get(0).getSurgeFactors().iterator().next().getValue(), is(closeTo(BigDecimal.valueOf(2.0d), BigDecimal.ZERO)));
    verify(notificationService).notifyUsers(argument.capture());
  }

  @Test
  public void shouldThrowsConflictExceptionOnEditingAreaWithExistingName() throws RideAustinException {
    // given
    expectedException.expect(ConflictException.class);
    SurgeArea surgeArea = SurgePricingTestUtils.mockSurgeArea(BigDecimal.valueOf(2.0d), false);
    surgeArea.setName(EXISTING_NAME);
    SurgeArea existing = SurgePricingTestUtils.mockSurgeArea(BigDecimal.ONE, false);
    existing.setId(SURGE_AREA_ID);
    when(surgeAreaDslRepository.findOne(anyLong())).thenReturn(existing);
    when(surgeAreaDslRepository.findByAreaName(eq(EXISTING_NAME))).thenReturn(new SurgeArea());

    // when
    surgePricingService.updateSurgeArea(SURGE_AREA_ID, surgeArea, true);
  }

  @Test
  public void testRemoveSurgeAreaSetsActiveToFalse() throws NotFoundException {
    SurgeArea existing = SurgePricingTestUtils.mockSurgeArea(BigDecimal.ONE);
    existing.setId(SURGE_AREA_ID);
    when(surgeAreaDslRepository.findOne(SURGE_AREA_ID)).thenReturn(existing);

    surgePricingService.removeSurgeArea(SURGE_AREA_ID);

    assertFalse(existing.isActive());
    verify(surgeAreaDslRepository, times(1)).save(eq(existing));
  }

  @Test(expected = NotFoundException.class)
  public void testRemoveSurgeAreaThrowsNFEOnNonExistentArea() throws NotFoundException {
    when(surgeAreaDslRepository.findOne(eq(SURGE_AREA_ID))).thenReturn(null);

    surgePricingService.removeSurgeArea(SURGE_AREA_ID);
  }

  @Test
  public void testCreateAreaWithExistingNameThrowsConflictException() throws RideAustinException {
    expectedException.expect(ConflictException.class);
    SurgeArea surgeArea = new SurgeArea();
    AreaGeometry areaGeometry = new AreaGeometry();
    areaGeometry.setCsvGeometry("1,1 2,2 3,3");
    surgeArea.setName(EXISTING_NAME);
    surgeArea.setAreaGeometry(areaGeometry);
    when(surgeAreaDslRepository.findByAreaName(eq(EXISTING_NAME))).thenReturn(new SurgeArea());

    surgePricingService.createSurgeArea(surgeArea);
  }

  @Test
  @UseDataProvider("surgeAreasForMaximumFactor")
  public void testSelectSurgeAreaByFactor(Triple<List<RedisSurgeArea>, Integer, BigDecimal> data) throws RideAustinException {
    Optional<RedisSurgeArea> result = surgePricingService.selectSurgeAreaByFactor(data.getLeft(), mockCarType(data.getMiddle()));

    assertTrue(result.isPresent());
    assertThat(result.get().getSurgeMapping().get(CAR_TYPE_MAPPING.get(data.getMiddle())), is(closeTo(data.getRight(), BigDecimal.ZERO)));
  }

  private CarType mockCarType(int bitmask) {
    CarType ct = new CarType();
    ct.setBitmask(bitmask);
    ct.setCarCategory(CAR_TYPE_MAPPING.get(bitmask));
    return ct;
  }

  private static RedisSurgeArea mockSurgeArea(Set<String> carCategories, int carCategoriesBitmask, double surgeFactor) {
    RedisSurgeArea sa = new RedisSurgeArea();
    sa.setCarCategoriesBitmask(carCategoriesBitmask);
    sa.setCityId(1L);
    for (String carCategory : carCategories) {
      sa.getSurgeMapping().put(carCategory, BigDecimal.valueOf(surgeFactor));
    }
    return sa;
  }

}