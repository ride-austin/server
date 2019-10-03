package com.rideaustin.assemblers;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

import com.rideaustin.model.Campaign;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.rest.model.ConsoleRideDto;
import com.rideaustin.service.CampaignService;
import com.rideaustin.service.CarTypeService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AdminRideDtoEnricher implements DTOEnricher<ConsoleRideDto> {

  private final CampaignService campaignService;
  private final CarTypeService carTypeService;
  private final StateMachinePersist<States, Events, String> contextAccess;
  private final Environment environment;

  @Override
  public ConsoleRideDto enrich(ConsoleRideDto source) {
    if (source == null) {
      return source;
    }
    StateMachineContext<States, Events> persistedContext = StateMachineUtils.getPersistedContext(environment, contextAccess, source.getId());
    if (persistedContext != null) {
      ExtendedState extendedState = persistedContext.getExtendedState();
      RideFlowContext flowContext = StateMachineUtils.getFlowContext(extendedState);
      if (source.getDriverAcceptedOn() == null) {
        source.setDriverAcceptedOn(flowContext.getAcceptedOn());
      }
      if (source.getDriverReachedOn() == null) {
        source.setDriverReachedOn(flowContext.getReachedOn());
      }
      if (source.getStartedOn() == null) {
        source.setStartedOn(flowContext.getStartedOn());
      }
    }
    Optional<CityCarType> cityCarType = carTypeService.getCityCarTypeWithFallback(source.getRequestedCarType().getCarCategory(), source.getCityId());
    cityCarType.ifPresent(cct -> source.getRequestedCarType().fillInfo(cct));

    if (source.getStatus() == RideStatus.COMPLETED) {
      final Optional<Campaign> campaign = campaignService.findExistingCampaignForRide(source.getId());
      campaign.ifPresent(c -> {
        source.setCampaignCoverage(source.getTotalCharge().minus(c.adjustTotalCharge(source.getTotalCharge())));
        source.setTotalChargeOverride(c.adjustTotalCharge(source.getTotalCharge()));
        source.setCampaign(c.getName());
      });
    }

    return source;
  }
}
