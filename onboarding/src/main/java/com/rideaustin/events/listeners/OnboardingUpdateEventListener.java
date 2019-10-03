package com.rideaustin.events.listeners;

import static com.rideaustin.service.onboarding.OnboardingStatusCheck.Result.FINAL_REVIEW;
import static com.rideaustin.service.onboarding.OnboardingStatusCheck.Result.NOT_CHANGED;
import static com.rideaustin.service.onboarding.OnboardingStatusCheck.Result.TERMINAL_STATUSES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import com.rideaustin.events.OnboardingUpdateEvent;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.onboarding.OnboardingStatusCheck;
import com.rideaustin.utils.DriverUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class OnboardingUpdateEventListener {

  private final DocumentDslRepository documentDslRepository;
  private final CarDocumentDslRepository carDocumentDslRepository;
  private final DriverDslRepository driverDslRepository;
  private final CurrentUserService currentUserService;
  private final List<OnboardingStatusCheck> onboardingStatusChecks = new ArrayList<>();

  @EventListener
  public void onStartUp(ContextRefreshedEvent event) {
    Collection<OnboardingStatusCheck> checks = event.getApplicationContext().getBeansOfType(OnboardingStatusCheck.class)
      .values();
    this.onboardingStatusChecks.addAll(checks
      .stream()
      .sorted(Comparator.comparing(OnboardingStatusCheck::getOrder))
      .collect(Collectors.toList())
    );
  }

  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public <T> void handleDriverUpdate(OnboardingUpdateEvent<T> event) {
    Driver driver = driverDslRepository.findById(event.getDriverId());
    fillEventContext(event, driver);
    T old = event.getOld();
    T updated = event.getUpdated();
    OnboardingStatusCheck.Result newOnboardingStatus = resolveOnboardingStatus(old, updated, event.getContext());
    updateDriverOnboardingStatus(newOnboardingStatus, driver);
  }

  private <T> void fillEventContext(OnboardingUpdateEvent<T> event, Driver driver) {
    List<Car> cars = DriverUtils.getActiveCars(driver);
    List<Document> documents = documentDslRepository.findDocumentsByAvatarsAndTypes(Collections.singleton(driver),
      EnumSet.of(DocumentType.LICENSE, DocumentType.DRIVER_PHOTO))
      .values()
      .stream()
      .map(Map::values)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
    documents.addAll(carDocumentDslRepository.findCarsDocuments(DriverUtils.getActiveCars(driver)));
    event.fillContext(driver, cars, documents);
  }

  private <T> OnboardingStatusCheck.Result resolveOnboardingStatus(T old, T updated, OnboardingUpdateEvent.Context eventContext) {
    OnboardingStatusCheck.Result newOnboardingStatus = NOT_CHANGED;
    for (OnboardingStatusCheck check : onboardingStatusChecks) {
      if (TERMINAL_STATUSES.contains(newOnboardingStatus)) {
        break;
      }
      if (check.supports(updated.getClass())) {
        OnboardingStatusCheck.Result result = check.check(old, updated, createCheckContext(eventContext, newOnboardingStatus));
        if (result == NOT_CHANGED) {
          newOnboardingStatus = check.currentValue(updated, createCheckContext(eventContext, newOnboardingStatus));
        } else {
          newOnboardingStatus = result;
        }
      }
    }
    if (!TERMINAL_STATUSES.contains(newOnboardingStatus)) {
      newOnboardingStatus = checkContext(eventContext, createCheckContext(eventContext, newOnboardingStatus));
    }
    return newOnboardingStatus;
  }

  private OnboardingStatusCheck.Context createCheckContext(OnboardingUpdateEvent.Context eventContext, OnboardingStatusCheck.Result newOnboardingStatus) {
    return new OnboardingStatusCheck.Context(eventContext.getDriver().getOnboardingStatus(), newOnboardingStatus);
  }

  private OnboardingStatusCheck.Result checkContext(OnboardingUpdateEvent.Context eventContext, OnboardingStatusCheck.Context checkContext) {
    OnboardingStatusCheck.Result driverResult = checkContextItem(eventContext.getDriver(), checkContext);
    if (TERMINAL_STATUSES.contains(driverResult)) {
      return driverResult;
    }
    OnboardingStatusCheck.Result carsResult = checkContextItems(eventContext.getCars(), checkContext);
    if (TERMINAL_STATUSES.contains(carsResult)) {
      return carsResult;
    }
    OnboardingStatusCheck.Result documentsResult = checkContextItems(eventContext.getDocuments(), checkContext);
    if (TERMINAL_STATUSES.contains(documentsResult)) {
      return documentsResult;
    }
    if (driverResult == FINAL_REVIEW && carsResult == FINAL_REVIEW && documentsResult == FINAL_REVIEW) {
      return FINAL_REVIEW;
    }
    return NOT_CHANGED;
  }

  private <T> OnboardingStatusCheck.Result checkContextItem(T item, OnboardingStatusCheck.Context checkContext) {
    OnboardingStatusCheck.Result result = NOT_CHANGED;
    for (OnboardingStatusCheck check : onboardingStatusChecks) {
      if (check.supports(item.getClass())) {
        result = check.currentValue(item, checkContext);
        if (TERMINAL_STATUSES.contains(result)) {
          break;
        }
      }
    }
    return result;
  }

  private <T> OnboardingStatusCheck.Result checkContextItems(Collection<T> items, OnboardingStatusCheck.Context checkContext) {
    OnboardingStatusCheck.Result result = NOT_CHANGED;
    for (T item : items) {
      result = checkContextItem(item, checkContext);
      if (TERMINAL_STATUSES.contains(result)) {
        break;
      }
    }
    return result;
  }

  private void updateDriverOnboardingStatus(OnboardingStatusCheck.Result status, Driver driver) {
    if (status != NOT_CHANGED) {
      switch (status) {
        case FINAL_REVIEW:
          driver.setOnboardingStatus(DriverOnboardingStatus.FINAL_REVIEW);
          driver.setOnboardingPendingSince(null);
          break;
        case PENDING:
          if (!DriverOnboardingStatus.PENDING.equals(driver.getOnboardingStatus())) {
            driver.setOnboardingStatus(DriverOnboardingStatus.PENDING);
            driver.setOnboardingPendingSince(new Date());
          }
          break;
        default:
          driver.setOnboardingStatus(status.status());
          break;
      }
      driverDslRepository.saveAs(driver, currentUserService.getUser());
    }
  }
}
