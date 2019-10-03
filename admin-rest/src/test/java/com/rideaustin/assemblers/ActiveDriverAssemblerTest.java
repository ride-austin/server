package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.assemblers.MapInfoDtoEnricher.ActiveDriverAssembler;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.rest.model.MapInfoDto.ActiveDriverInfo;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.OnlineDriverDto;

public class ActiveDriverAssemblerTest {

  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;

  private ActiveDriverAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ActiveDriverAssembler(requestedDriversRegistry);
  }

  @Test
  public void toDtoSetsRequestedStatus() {
    when(requestedDriversRegistry.isRequested(anyLong())).thenReturn(true);
    final OnlineDriverDto source = new OnlineDriverDto();
    final LocationObject location = new LocationObject();
    final double latitude = 34.6481616;
    final double longitude = -97.6161;
    location.setLatitude(latitude);
    location.setLongitude(longitude);
    source.setLocationObject(location);

    final ActiveDriverInfo result = testedInstance.toDto(source);

    assertEquals(ActiveDriverStatus.REQUESTED, result.getStatus());
    assertEquals(latitude, result.getLatitude(), 0.0);
    assertEquals(longitude, result.getLongitude(), 0.0);
    assertEquals(source.getFullName(), result.getDriver().getFullName());
    assertEquals(source.getPhoneNumber(), result.getDriver().getPhoneNumber());
    assertEquals(source.getDriverId(), result.getDriver().getId());
  }

  @Test
  public void toDtoSetsSourceStatus() {
    when(requestedDriversRegistry.isRequested(anyLong())).thenReturn(false);
    final OnlineDriverDto source = new OnlineDriverDto();
    final LocationObject location = new LocationObject();
    final double latitude = 34.6481616;
    final double longitude = -97.6161;
    location.setLatitude(latitude);
    location.setLongitude(longitude);
    source.setLocationObject(location);
    source.setStatus(ActiveDriverStatus.RIDING);

    final ActiveDriverInfo result = testedInstance.toDto(source);

    assertEquals(ActiveDriverStatus.RIDING, result.getStatus());
    assertEquals(latitude, result.getLatitude(), 0.0);
    assertEquals(longitude, result.getLongitude(), 0.0);
    assertEquals(source.getFullName(), result.getDriver().getFullName());
    assertEquals(source.getPhoneNumber(), result.getDriver().getPhoneNumber());
    assertEquals(source.getDriverId(), result.getDriver().getId());
  }
}
