package com.rideaustin.events.listeners;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import com.rideaustin.events.TNCCardUpdateEvent;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.service.CurrentUserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TNCCardUpdateEventListener {

  private final DocumentDslRepository documentDslRepository;
  private final DriverDslRepository driverDslRepository;
  private final CurrentUserService currentUserService;

  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleTNCCardUpdateEvent(TNCCardUpdateEvent event) {
    CityApprovalStatus newStatus = resolveNewStatus(event);
    if (newStatus != null) {
      Driver driver = documentDslRepository.findDriver(event.getCard());
      driver.setCityApprovalStatus(newStatus);
      driverDslRepository.saveAs(driver, currentUserService.getUser());
    }
  }

  private CityApprovalStatus resolveNewStatus(TNCCardUpdateEvent event) {
    CityApprovalStatus newStatus;
    switch (event.getStatus()) {
      case APPROVED:
        newStatus = CityApprovalStatus.APPROVED;
        break;
      case EXPIRED:
        newStatus = CityApprovalStatus.EXPIRED;
        break;
      case PENDING:
        newStatus = CityApprovalStatus.PENDING;
        break;
      case REJECTED:
        newStatus = CityApprovalStatus.REJECTED_BY_CITY;
        break;
      default:
        newStatus = CityApprovalStatus.APPROVED;
    }
    return newStatus;
  }
}
