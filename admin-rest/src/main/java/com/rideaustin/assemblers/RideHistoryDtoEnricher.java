package com.rideaustin.assemblers;

import javax.inject.Inject;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

import com.rideaustin.rest.model.RideHistoryDto;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideHistoryDtoEnricher implements DTOEnricher<RideHistoryDto>, Converter<RideHistoryDto, RideHistoryDto> {

  private final StateMachinePersist<States, Events, String> contextAccess;
  private final Environment environment;

  @Override
  public RideHistoryDto enrich(RideHistoryDto source) {
    if (source == null) {
      return null;
    }
    StateMachineContext<States, Events> persistedContext = StateMachineUtils.getPersistedContext(environment, contextAccess, source.getId());
    if (persistedContext != null) {
      ExtendedState extendedState = persistedContext.getExtendedState();
      RideFlowContext flowContext = StateMachineUtils.getFlowContext(extendedState);
      if (source.getStartedOn() == null) {
        source.setStartedOn(flowContext.getStartedOn());
      }
    }
    return source;
  }

  @Override
  public RideHistoryDto convert(RideHistoryDto source) {
    return enrich(source);
  }
}
