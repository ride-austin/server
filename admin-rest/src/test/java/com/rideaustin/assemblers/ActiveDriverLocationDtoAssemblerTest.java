package com.rideaustin.assemblers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.rest.model.ActiveDriverLocationDto;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;

public class ActiveDriverLocationDtoAssemblerTest {

  private static final String CAR_CATEGORY = "REGULAR";

  private ActiveDriverLocationDtoAssembler testedInstance;
  @Mock
  private CarTypesCache carTypesCache;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(carTypesCache.fromBitMask(anyInt())).thenReturn(Collections.singleton(CAR_CATEGORY));
    CarTypesUtils.setCarTypesCache(carTypesCache);
    testedInstance = new ActiveDriverLocationDtoAssembler();
  }

  @Test
  public void toDtoSkipsNull() {
    final ActiveDriverLocationDto result = testedInstance.toDto((OnlineDriverDto) null);

    assertNull(result);
  }

  @Test
  public void toDto() {
    final OnlineDriverDto source = new OnlineDriverDto();
    final LocationObject locationObject = new LocationObject();
    final double latitude = 34.4564;
    final double longitude = -97.4564;
    locationObject.setLatitude(latitude);
    locationObject.setLongitude(longitude);
    source.setLocationObject(locationObject);
    source.setAvailableCarCategoriesBitmask(1);

    final ActiveDriverLocationDto result = testedInstance.toDto(source);

    assertEquals(latitude, result.getLat(), 0.0);
    assertEquals(longitude, result.getLng(), 0.0);
    assertThat(result.getCarCategories()).containsExactly(CAR_CATEGORY);
  }
}