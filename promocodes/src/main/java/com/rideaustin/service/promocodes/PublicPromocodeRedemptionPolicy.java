package com.rideaustin.service.promocodes;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.repo.dsl.PromocodeRedemptionDslRepository;
import com.rideaustin.service.generic.TimeService;

@Component
public class PublicPromocodeRedemptionPolicy extends PromocodeRedemptionPolicy {

  @Inject
  public PublicPromocodeRedemptionPolicy(TimeService timeService, PromocodeRedemptionDslRepository promocodeRedemptionDslRepository) {
    super(timeService, promocodeRedemptionDslRepository);
  }
}
