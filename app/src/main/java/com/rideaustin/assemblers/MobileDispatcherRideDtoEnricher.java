package com.rideaustin.assemblers;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.DispatcherAccountRideDto;
import com.rideaustin.service.CarTypeService;
import com.rideaustin.service.MapService;
import com.rideaustin.service.UpdateDistanceTimeService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.ride.RideUpgradeService;

@Component
public class MobileDispatcherRideDtoEnricher extends MobileRiderRideDtoEnricher<DispatcherAccountRideDto> {

  @Inject
  public MobileDispatcherRideDtoEnricher(StateMachinePersist<States, Events, String> contextAccess, Environment environment,
    RideUpgradeService upgradeService, ObjectLocationService<OnlineDriverDto> objectLocationService, RideDslRepository rideDslRepository,
    RidePaymentConfig paymentConfig, DocumentDslRepository documentDslRepository, CarDocumentDslRepository carDocumentDslRepository,
    MapService mapService, CityCarTypeDtoAssembler cityCarTypeDtoAssembler, UpdateDistanceTimeService updateDistanceTimeService,
    CarTypeService carTypeService) {
    super(contextAccess, environment, upgradeService, objectLocationService, rideDslRepository, paymentConfig, documentDslRepository,
      carDocumentDslRepository, mapService, cityCarTypeDtoAssembler, updateDistanceTimeService, carTypeService);
  }
}
