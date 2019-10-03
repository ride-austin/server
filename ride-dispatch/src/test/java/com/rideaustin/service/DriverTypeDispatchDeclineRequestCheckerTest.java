package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.dispatch.service.DriverTypeDispatchDeclineRequestChecker;
import com.rideaustin.dispatch.service.DriverTypeDispatchDeclineRequestChecker.DriverTypeDispatchDeclineActiveDriverData;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.user.DriverTypeCache;

@RunWith(MockitoJUnitRunner.class)
public class DriverTypeDispatchDeclineRequestCheckerTest {

  private DriverTypeDispatchDeclineRequestChecker checker;

  @Mock
  private DriverTypeCache driverTypeCache;
  @Mock
  private ObjectLocationService locationService;

  @Before
  public void setup() {
    ObjectMapper objectMapper = new ObjectMapper();
    checker = new DriverTypeDispatchDeclineRequestChecker(objectMapper, driverTypeCache, locationService);
  }

  @Test
  public void checkIfActiveDriverNeedAddConsecutiveDeclineRequestDuringRide() throws Exception {
    Ride ride = new Ride();

    boolean result = checker.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(new DriverTypeDispatchDeclineActiveDriverData(ActiveDriverStatus.RIDING), ride);
    assertThat(result, is(Boolean.FALSE));
  }


  @Test
  public void checkIfActiveDriverNeedAddConsecutiveDeclineRequestNoRequestedType() throws Exception {
    Ride ride = new Ride();
    CityDriverType cdt = new CityDriverType();

    when(driverTypeCache.getByCityAndBitmask(anyLong(), anyInt()))
      .thenReturn(Collections.singleton(cdt));
    boolean result = checker.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(new DriverTypeDispatchDeclineActiveDriverData(ActiveDriverStatus.AVAILABLE), ride);
    assertThat(result, is(Boolean.TRUE));

  }

  @Test
  public void checkIfActiveDriverNeedAddConsecutiveDeclineRequestNoConfiguration() throws Exception {
    Ride ride = new Ride();
    ride.setCityId(1L);
    CityDriverType cdt = new CityDriverType();
    DriverType dt = new DriverType();
    dt.setCityDriverTypes(new HashSet<>());
    dt.getCityDriverTypes().add(cdt);
    ride.setRequestedDriverTypeBitmask(1);
    when(driverTypeCache.getByCityAndBitmask(anyLong(), anyInt()))
      .thenReturn(Collections.singleton(cdt));

    boolean result = checker.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(new DriverTypeDispatchDeclineActiveDriverData(ActiveDriverStatus.AVAILABLE), ride);
    assertThat(result, is(Boolean.TRUE));

  }

  @Test
  public void checkIfActiveDriverNeedAddConsecutiveDeclineRequestPenalizeTrue() throws Exception {
    Ride ride = new Ride();
    ride.setCityId(1L);
    DriverType dt = new DriverType();
    CityDriverType cdt = new CityDriverType();
    cdt.setConfiguration("{\"penalizeDeclinedRides\":true}");
    cdt.setConfigurationClass(CityDriverType.DefaultDriverTypeConfiguration.class);
    dt.setCityDriverTypes(new HashSet<>());
    dt.getCityDriverTypes().add(cdt);
    ride.setRequestedDriverTypeBitmask(1);
    when(driverTypeCache.getByCityAndBitmask(anyLong(), anyInt()))
      .thenReturn(Collections.singleton(cdt));

    boolean result = checker.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(new DriverTypeDispatchDeclineActiveDriverData(ActiveDriverStatus.AVAILABLE), ride);
    assertThat(result, is(Boolean.TRUE));

  }

  @Test
  public void checkIfActiveDriverNeedAddConsecutiveDeclineRequestPenalizeFalse() throws Exception {
    Ride ride = new Ride();
    ride.setCityId(1L);
    DriverType dt = new DriverType();
    CityDriverType cdt = new CityDriverType();
    cdt.setConfiguration("{\"penalizeDeclinedRides\":false}");
    cdt.setConfigurationClass(CityDriverType.DefaultDriverTypeConfiguration.class);
    dt.setCityDriverTypes(new HashSet<>());
    dt.getCityDriverTypes().add(cdt);
    ride.setRequestedDriverTypeBitmask(1);
    ride.setRequestedCarType(new CarType());
    when(driverTypeCache.getByCityAndBitmask(anyLong(), anyInt()))
      .thenReturn(Collections.singleton(cdt));

    boolean result = checker.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(new DriverTypeDispatchDeclineActiveDriverData(ActiveDriverStatus.AVAILABLE), ride);
    assertThat(result, is(Boolean.FALSE));

  }

}