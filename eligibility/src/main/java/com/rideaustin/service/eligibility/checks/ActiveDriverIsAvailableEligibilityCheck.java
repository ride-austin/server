package com.rideaustin.service.eligibility.checks;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.EligibilityCheckError;

@Component
@EligibilityCheck(targetClass = ActiveDriver.class)
public class ActiveDriverIsAvailableEligibilityCheck extends BaseEligibilityCheckItem<ActiveDriver> {

  static final String MESSAGE = "Please complete the current ride.";

  @Inject
  private RideDslRepository rideDslRepository;
  @Inject
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Inject
  private ActiveDriverLocationService activeDriverLocationService;

  public ActiveDriverIsAvailableEligibilityCheck() {
    super(Collections.emptyMap());
  }

  @Override
  public Optional<EligibilityCheckError> check(ActiveDriver subject) {
    if (subject.getStatus() == ActiveDriverStatus.RIDING) {
      List<Ride> ongoingRides = rideDslRepository.findByActiveDriverAndStatuses(subject, RideStatus.ONGOING_DRIVER_STATUSES);
      if (!ongoingRides.isEmpty()) {
        return Optional.of(new EligibilityCheckError(MESSAGE));
      } else {
        activeDriverLocationService.updateActiveDriverLocationStatus(subject.getId(), ActiveDriverStatus.AVAILABLE);
        activeDriverDslRepository.setRidingDriverAsAvailable(subject.getId());
      }
    }
    return Optional.empty();
  }
}
