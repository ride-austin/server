package com.rideaustin.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.ServerError;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ActiveDriversDisableService {

  private final ActiveDriversService activeDriversService;
  private final DriverDslRepository driverDslRepository;
  private final RideFlowService rideFlowService;
  private final RideDslRepository rideDslRepo;

  public void disableActiveDriverImmediately(Long driverId) throws ServerError {
    Driver driver = driverDslRepository.findById(driverId);
    ActiveDriver currentActiveDriver = activeDriversService.getActiveDriverByDriver(driver.getUser());
    if (currentActiveDriver == null) {
      return;
    }
    if (ActiveDriverStatus.ONGOING_ACTIVE_DRIVER_STATUSES.contains(currentActiveDriver.getStatus())) {
      cancelRideIfExist(driver);
    }
    activeDriversService.deactivateWithMessage(currentActiveDriver, ActiveDriversService.GoOfflineEventSource.ADMIN_DISABLE);
  }

  private void cancelRideIfExist(Driver driver) {
    Ride ride = rideDslRepo.getOngoingRideForDriver(driver.getUser());
    if (ride != null) {
      rideFlowService.cancelAsAdmin(ride.getId());
    }
  }
}
