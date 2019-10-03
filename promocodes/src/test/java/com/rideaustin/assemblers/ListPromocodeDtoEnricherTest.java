package com.rideaustin.assemblers;

import static org.junit.Assert.*;
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

import com.rideaustin.rest.model.ListPromocodeDto;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.user.CarTypesCache;

public class ListPromocodeDtoEnricherTest {

  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private CityCache cityCache;

  private ListPromocodeDtoEnricher testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ListPromocodeDtoEnricher(carTypesCache, cityCache);
  }

  @Test
  public void enrichSkipsNull() {
    final ListPromocodeDto result = testedInstance.enrich(null);

    assertNull(result);
  }

  @Test
  public void enrichSetsCarsAndCities() {
    final int cityBitmask = 1;
    final int carTypesBitmask = 1;
    ListPromocodeDto source = new ListPromocodeDto(1L, "A", "B", BigDecimal.TEN, new Date(), null,
      false, null, null, null, cityBitmask, carTypesBitmask,
      null, null, null, false, false, BigDecimal.ZERO);
    final Set<Long> cities = Collections.singleton(1L);
    final Set<String> carTypes = Collections.singleton("REGULAR");
    when(cityCache.fromBitMask(eq(cityBitmask))).thenReturn(cities);
    when(carTypesCache.fromBitMask(eq(carTypesBitmask))).thenReturn(carTypes);

    final ListPromocodeDto result = testedInstance.enrich(source);

    assertTrue(CollectionUtils.isEqualCollection(cities, result.getCities()));
    assertTrue(CollectionUtils.isEqualCollection(carTypes, result.getCarTypes()));
  }

  @Test
  public void enrichSetsMaxPromoValue() {
    final long maximumRedemption = 10L;
    final BigDecimal codeValue = BigDecimal.TEN;
    final BigDecimal expected = codeValue.multiply(BigDecimal.valueOf(maximumRedemption));
    ListPromocodeDto source = new ListPromocodeDto(1L, "A", "B", codeValue, new Date(), null,
      false, maximumRedemption, null, null, 1, 1,
      null, null, null, false, false, BigDecimal.ZERO);

    final ListPromocodeDto result = testedInstance.enrich(source);

    assertEquals(expected, result.getMaxPromotionValue());
  }
}