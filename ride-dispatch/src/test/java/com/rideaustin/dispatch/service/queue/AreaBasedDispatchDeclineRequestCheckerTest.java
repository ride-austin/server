package com.rideaustin.dispatch.service.queue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.maps.model.LatLng;
import com.rideaustin.dispatch.service.queue.AreaBasedDispatchDeclineRequestChecker.AreaBasedDispatchDeclineActiveDriverData;
import com.rideaustin.model.Area;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.model.OnlineDriverDto;

public class AreaBasedDispatchDeclineRequestCheckerTest {

  @Mock
  private AreaService areaService;
  @Mock
  private ObjectLocationService<OnlineDriverDto> locationService;

  private AreaBasedDispatchDeclineRequestChecker testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new AreaBasedDispatchDeclineRequestChecker(areaService, locationService);
  }

  @Test
  public void checkReturnsFalseOnAbsentData() {
    final boolean result = testedInstance.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(Optional.empty(), new Ride());

    assertFalse(result);
  }

  @Test
  public void checkReturnsFalseWhenRideNotInAreaAndDriverIsInArea() {
    AreaBasedDispatchDeclineActiveDriverData data = new AreaBasedDispatchDeclineActiveDriverData(new LatLng(34.684681, -97.984691));
    when(areaService.isInArea(any(Ride.class))).thenReturn(null);
    when(areaService.isInArea(any(LatLng.class), anyLong())).thenReturn(new Area());

    final boolean result = testedInstance.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(Optional.of(data), new Ride());

    assertFalse(result);
  }

  @Test
  public void checkReturnsFalseWhenRideAndDriverAreInDifferentAreas() {
    AreaBasedDispatchDeclineActiveDriverData data = new AreaBasedDispatchDeclineActiveDriverData(new LatLng(34.684681, -97.984691));
    final Area rideArea = new Area();
    rideArea.setId(1L);
    when(areaService.isInArea(any(Ride.class))).thenReturn(rideArea);
    final Area driverArea = new Area();
    driverArea.setId(2L);
    when(areaService.isInArea(any(LatLng.class), anyLong())).thenReturn(driverArea);

    final boolean result = testedInstance.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(Optional.of(data), new Ride());

    assertFalse(result);
  }

  @Test
  public void checkReturnsTrueWhenRideAndDriverAreInSameArea() {
    AreaBasedDispatchDeclineActiveDriverData data = new AreaBasedDispatchDeclineActiveDriverData(new LatLng(34.684681, -97.984691));
    final Area area = new Area();
    area.setId(1L);
    when(areaService.isInArea(any(Ride.class))).thenReturn(area);
    when(areaService.isInArea(any(LatLng.class), anyLong())).thenReturn(area);

    final boolean result = testedInstance.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(Optional.of(data), new Ride());

    assertTrue(result);
  }
}