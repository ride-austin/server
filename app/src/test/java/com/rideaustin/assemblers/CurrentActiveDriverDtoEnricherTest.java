package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.rest.model.CurrentActiveDriverDto;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesCache;

public class CurrentActiveDriverDtoEnricherTest {

  @Mock
  private ObjectLocationService<OnlineDriverDto> objectLocationService;
  @Mock
  private CarTypesCache carTypesCache;

  private CurrentActiveDriverDtoEnricher testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CurrentActiveDriverDtoEnricher(objectLocationService, carTypesCache);
  }

  @Test
  public void enrichSkipsNull() {
    final CurrentActiveDriverDto result = testedInstance.enrich(null);

    assertNull(result);
  }

  @Test
  public void enrichSetsLocation() {
    final long sourceId = 1L;
    CurrentActiveDriverDto source = new CurrentActiveDriverDto(sourceId, 1L, 1L, ActiveDriverStatus.AVAILABLE,
      1);
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    final double lat = 34.146151;
    final double lng = -97.646161;
    onlineDriver.setLocationObject(new LocationObject(lat, lng));
    when(objectLocationService.getById(eq(source.getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);

    final CurrentActiveDriverDto result = testedInstance.enrich(source);

    assertEquals(lat, result.getLatitude(), 0.0);
    assertEquals(lng, result.getLongitude(), 0.0);
  }

  @Test
  public void enrichSetsCarCategories() {
    CurrentActiveDriverDto source = new CurrentActiveDriverDto(1L, 1L, 1L, ActiveDriverStatus.AVAILABLE,
      1);
    final Set<String> carTypes = Collections.singleton("REGULAR");
    when(carTypesCache.fromBitMask(eq(1))).thenReturn(carTypes);

    final CurrentActiveDriverDto result = testedInstance.enrich(source);

    assertEquals(carTypes, result.getCarCategories());
  }
}