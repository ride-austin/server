package com.rideaustin.jobs;

import static java.util.stream.Collectors.toSet;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.BaseEntity;
import com.rideaustin.repo.dsl.CityDslRepository;
import com.rideaustin.service.surgepricing.SurgeRecalculationServiceFactory;

@Component
public class SurgePricingAreasJob extends BaseJob {

  @Inject
  private SurgeRecalculationServiceFactory recalculationServiceFactory;
  @Inject
  private CityDslRepository cityDslRepository;

  @Override
  protected void executeInternal() {
    Set<Long> cities = cityDslRepository.findAll().stream().map(BaseEntity::getId).collect(toSet());
    for (Long city : cities) {
      recalculationServiceFactory
        .createRecalculationService(city)
        .recalculateSurgePricingAreas(city);
    }
  }

  @Override
  protected String getDescription() {
    return "Recalculating Surge Pricing Areas";
  }

}
