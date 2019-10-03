package com.rideaustin.service.eligibility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.reflections.Reflections;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.eligibility.checks.EligibilityCheckItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public abstract class BaseEligibilityCheckService<T extends EligibilityCheckContext> {

  protected final ConfigurationItemCache configurationCache;

  private final BeanFactory beanFactory;
  private final CurrentUserService currentUserService;

  static final Set<Class<?>> REGISTERED_CHECKS = new Reflections(EligibilityCheckItem.class.getPackage().getName()).getTypesAnnotatedWith(EligibilityCheck.class);

  public void check(T context, Long cityId) throws RideAustinException {
    Set<Class<?>> checks = new HashSet<>();
    Set<String> classNames = new HashSet<>();
    if (!context.getRequestedChecks().isEmpty()) {
      checks = context.getRequestedChecks();
    } else {
      classNames = getDefaultChecks(cityId);
    }

    Map<String, Object> contextParams = context.getParams();
    doCheck(context, checks, classNames, contextParams);
  }

  protected abstract void doCheck(T context, Set<Class<?>> checks, Set<String> classNames, Map<String, Object> contextParams) throws BadRequestException;

  protected abstract Set<String> getDefaultChecks(Long cityId) throws ServerError;

  protected final <E> void performCheck(Set<EligibilityCheckItem<E>> checks, E subject) throws BadRequestException {
    ArrayList<EligibilityCheckItem<E>> checksList = new ArrayList<>(checks);
    checksList.sort(Comparator.comparing(EligibilityCheckItem::getOrder));
    for (EligibilityCheckItem<E> check : checksList) {
      Optional<EligibilityCheckError> error = check.check(subject);
      if (error.isPresent()) {
        log.error(String.format("User #%s eligibility check resulted in %s", currentUserService.getUser().getId(), error.get().getMessage()));
        throw new BadRequestException(error.get().getMessage());
      }
    }
  }

  protected final <E> Set<EligibilityCheckItem<E>> getCheckBeans(Set<Class<?>> checkClasses, Map<String, Object> contextParams) {
    Set<EligibilityCheckItem<E>> checks = new HashSet<>();
    for (Class driverCheckClass : checkClasses) {
      EligibilityCheck metadata = (EligibilityCheck) driverCheckClass.getAnnotation(EligibilityCheck.class);
      if (metadata.contextAware()) {
        checks.add((EligibilityCheckItem<E>) beanFactory.getBean(driverCheckClass, contextParams));
      } else {
        checks.add((EligibilityCheckItem<E>) beanFactory.getBean(driverCheckClass));
      }
    }
    return checks;
  }

  protected final Set<Class<?>> getChecksFor(Collection<Class<?>> checks, Set<String> classNames, Class subjectClass) {
    Stream<Class<?>> classStream = checks.stream();
    if (checks.isEmpty() && !classNames.isEmpty()) {
      classStream = getRegisteredChecks().stream().filter(c -> classNames.contains(c.getName()));
    }
    return classStream.filter(c -> filterChecksBySubject(c, subjectClass)).collect(Collectors.toSet());
  }

  protected Set<Class<?>> getRegisteredChecks() {
    return REGISTERED_CHECKS;
  }

  private boolean filterChecksBySubject(Class checkClass, Class subjectClass) {
    EligibilityCheck metadata = (EligibilityCheck) checkClass.getAnnotation(EligibilityCheck.class);
    return metadata.targetClass().equals(subjectClass);
  }
}
