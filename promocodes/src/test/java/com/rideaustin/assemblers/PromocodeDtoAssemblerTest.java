package com.rideaustin.assemblers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.promocodes.PromocodeType;
import com.rideaustin.rest.model.PromocodeDto;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;

public class PromocodeDtoAssemblerTest {

  @Mock
  private CityCache cityCache;
  @Mock
  private CarTypesCache carTypesCache;

  private PromocodeDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    CarTypesUtils.setCarTypesCache(carTypesCache);

    testedInstance = new PromocodeDtoAssembler(cityCache);
  }

  @Test
  public void toDtoSkipsNull() {
    final PromocodeDto result = testedInstance.toDto((Promocode) null);

    assertNull(result);
  }

  @Test
  public void toDtoFillsData() {
    Promocode source = new Promocode();
    source.setId(1L);
    source.setPromocodeType(PromocodeType.PUBLIC);
    source.setCodeLiteral("A");
    source.setCodeValue(BigDecimal.TEN);
    source.setTitle("B");
    source.setStartsOn(new Date());
    source.setEndsOn(new Date());
    source.setNewRidersOnly(false);
    source.setMaximumRedemption(1L);
    source.setMaximumUsesPerAccount(1);
    source.setCurrentRedemption(0L);
    source.setDriverId(null);
    source.setCarTypeBitmask(1);
    source.setCityBitmask(1);
    source.setNextTripOnly(false);
    source.setUseEndDate(new Date());
    source.setApplicableToFees(false);
    source.setValidForNumberOfDays(1);
    source.setValidForNumberOfRides(null);
    source.setCappedAmountPerUse(BigDecimal.ONE);

    final Set<Long> cities = Collections.singleton(1L);
    final Set<String> carTypes = Collections.singleton("REGULAR");
    when(carTypesCache.fromBitMask(eq(1))).thenReturn(carTypes);
    when(cityCache.fromBitMask(eq(1))).thenReturn(cities);

    final PromocodeDto result = testedInstance.toDto(source);

    assertEquals(source.getId(), result.getId().longValue());
    assertEquals(source.getPromocodeType(), result.getPromocodeType());
    assertEquals(source.getCodeLiteral(), result.getCodeLiteral());
    assertEquals(source.getCodeValue(), result.getCodeValue());
    assertEquals(source.getTitle(), result.getTitle());
    assertEquals(source.getStartsOn(), result.getStartsOn());
    assertEquals(source.getEndsOn(), result.getEndsOn());
    assertEquals(source.isNewRidersOnly(), result.isNewRidersOnly());
    assertEquals(source.getMaximumRedemption(), result.getMaximumRedemption());
    assertEquals(source.getMaximumUsesPerAccount(), result.getMaximumUsesPerAccount());
    assertEquals(source.getCurrentRedemption(), result.getCurrentRedemption());
    assertEquals(source.getDriverId(), result.getDriverId());
    assertTrue(CollectionUtils.isEqualCollection(carTypes, result.getCarTypes()));
    assertTrue(CollectionUtils.isEqualCollection(cities, result.getCities()));
    assertEquals(source.isNextTripOnly(), result.isNextTripOnly());
    assertEquals(source.getUseEndDate(), result.getUseEndDate());
    assertEquals(source.isApplicableToFees(), result.isApplicableToFees());
    assertEquals(source.getValidForNumberOfDays(), result.getValidForNumberOfDays());
    assertEquals(source.getValidForNumberOfRides(), result.getValidForNumberOfRides());
    assertEquals(source.getCappedAmountPerUse(), result.getCappedAmountPerUse());

  }

  @Test
  public void toDsFillsData() {
    final Set<Long> cities = Collections.singleton(1L);
    final Set<String> carTypes = Collections.singleton("REGULAR");

    PromocodeDto source = new PromocodeDto();
    source.setId(1L);
    source.setPromocodeType(PromocodeType.PUBLIC);
    source.setCodeLiteral("A");
    source.setCodeValue(BigDecimal.TEN);
    source.setTitle("B");
    source.setStartsOn(new Date());
    source.setEndsOn(new Date());
    source.setNewRidersOnly(false);
    source.setMaximumRedemption(1L);
    source.setMaximumUsesPerAccount(1);
    source.setCurrentRedemption(0L);
    source.setDriverId(null);
    source.setCarTypes(carTypes);
    source.setCities(cities);
    source.setNextTripOnly(false);
    source.setUseEndDate(new Date());
    source.setApplicableToFees(false);
    source.setValidForNumberOfDays(1);
    source.setValidForNumberOfRides(null);
    source.setCappedAmountPerUse(BigDecimal.ONE);

    when(carTypesCache.toBitMask(anySetOf(String.class))).thenReturn(1);
    when(cityCache.toBitMask(anySetOf(Long.class))).thenReturn(1);

    final Promocode result = testedInstance.toDs(source);

    assertEquals(source.getId().longValue(), result.getId());
    assertEquals(source.getPromocodeType(), result.getPromocodeType());
    assertEquals(source.getCodeLiteral(), result.getCodeLiteral());
    assertEquals(source.getCodeValue(), result.getCodeValue());
    assertEquals(source.getTitle(), result.getTitle());
    assertEquals(source.getStartsOn(), result.getStartsOn());
    assertEquals(source.getEndsOn(), result.getEndsOn());
    assertEquals(source.isNewRidersOnly(), result.isNewRidersOnly());
    assertEquals(source.getMaximumRedemption(), result.getMaximumRedemption());
    assertEquals(source.getMaximumUsesPerAccount(), result.getMaximumUsesPerAccount());
    assertNull(result.getCurrentRedemption());
    assertEquals(source.getDriverId(), result.getDriverId());
    assertEquals(1, result.getCarTypeBitmask().intValue());
    assertEquals(1, result.getCityBitmask().intValue());
    assertEquals(source.isNextTripOnly(), result.isNextTripOnly());
    assertEquals(source.getUseEndDate(), result.getUseEndDate());
    assertEquals(source.isApplicableToFees(), result.isApplicableToFees());
    assertEquals(source.getValidForNumberOfDays(), result.getValidForNumberOfDays());
    assertEquals(source.getValidForNumberOfRides(), result.getValidForNumberOfRides());
    assertEquals(source.getCappedAmountPerUse(), result.getCappedAmountPerUse());

  }
}