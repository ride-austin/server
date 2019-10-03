package com.rideaustin.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.ServerError;

public class ActiveDriversDisableServiceTest {

  @Mock
  private ActiveDriversService activeDriversService;
  @Mock
  private DriverDslRepository driverDslRepository;
  @Mock
  private RideFlowService rideFlowService;
  @Mock
  private RideDslRepository rideDslRepo;

  private ActiveDriversDisableService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ActiveDriversDisableService(activeDriversService, driverDslRepository, rideFlowService, rideDslRepo);
  }

  @Test
  public void disableActiveDriverImmediatelySkipsOfflineDriver() throws ServerError {
    final Driver driver = new Driver();
    when(driverDslRepository.findById(anyLong())).thenReturn(driver);

    testedInstance.disableActiveDriverImmediately(1L);

    verify(activeDriversService, never()).deactivateWithMessage(any(ActiveDriver.class), eq(ActiveDriversService.GoOfflineEventSource.ADMIN_DISABLE));
  }

  @Test
  public void disableActiveDriverImmediatelyCancelsOngoingRide() throws ServerError {
    final Driver driver = new Driver();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setStatus(ActiveDriverStatus.RIDING);
    when(driverDslRepository.findById(anyLong())).thenReturn(driver);
    when(activeDriversService.getActiveDriverByDriver(any(User.class))).thenReturn(activeDriver);
    when(rideDslRepo.getOngoingRideForDriver(any(User.class))).thenReturn(new Ride());

    testedInstance.disableActiveDriverImmediately(1L);

    verify(rideFlowService, times(1)).cancelAsAdmin(anyLong());
  }

  @Test
  public void disableActiveDriverImmediatelyDeactivatesDriver() throws ServerError {
    final Driver driver = new Driver();
    final ActiveDriver activeDriver = new ActiveDriver();
    when(driverDslRepository.findById(anyLong())).thenReturn(driver);
    when(activeDriversService.getActiveDriverByDriver(any(User.class))).thenReturn(activeDriver);

    testedInstance.disableActiveDriverImmediately(1L);

    verify(activeDriversService, times(1)).deactivateWithMessage(eq(activeDriver), eq(ActiveDriversService.GoOfflineEventSource.ADMIN_DISABLE));
  }
}