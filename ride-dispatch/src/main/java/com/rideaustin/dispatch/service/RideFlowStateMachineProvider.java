package com.rideaustin.dispatch.service;

import java.util.Optional;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import com.google.maps.model.LatLng;
import com.rideaustin.dispatch.KillInceptionMachineMessage;
import com.rideaustin.dispatch.ProxyEventMessage;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.DispatchType;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RideFlowStateMachineProvider {

  private final Environment environment;
  private final BeanFactory beanFactory;

  private final RideDispatchServiceConfig config;

  private final AreaService areaService;

  private final RideDslRepository rideDslRepository;

  private final StateMachinePersist<States, Events, String> persister;
  private final RedisTemplate<byte[], byte[]> redisTemplate;
  private final ChannelTopic inceptionMachineTopic;

  public StateMachine<States, Events> createMachine(RideRequestContext requestContext) {
    StateMachine<States, Events> machine = createMachine(true);
    machine.getExtendedState().getVariables().put("requestContext", requestContext);
    return machine;
  }

  public Optional<StateMachine<States, Events>> restoreMachine(long rideId, Events event, MessageHeaders headers) {
    Optional<StateMachine<States, Events>> restored = restoreMachine(rideId, event.causesInception(), event.killsInception());
    if (!restored.isPresent()) {
      restored = forceRestore(rideId, event, headers);
    }
    return restored;
  }

  public void sendProxiedEvent(long rideId, Events event, MessageHeaders headers) {
    redisTemplate.convertAndSend(inceptionMachineTopic.getTopic(), new ProxyEventMessage(rideId, event, headers));
  }

  private StateMachine<States, Events> createMachine(Ride ride, boolean forceRestore) {
    StateMachine<States, Events> machine = createMachine(true);
    RideRequestContext requestContext;
    if (forceRestore) {
      requestContext = rideDslRepository.getRequestContext(ride);
    } else {
      requestContext = StateMachineUtils.createRequestContext(ride, config.getDriverSearchRadiusStart());
    }

    machine.getExtendedState().getVariables().put("requestContext", requestContext);
    return machine;
  }

  private Optional<StateMachine<States, Events>> restoreMachine(long rideId, boolean inception, boolean killInception) {
    try {
      StateMachine<States, Events> machine = createMachine(inception);
      String machineId = StateMachineUtils.getMachineId(environment, rideId);

      if (killInception) {
        redisTemplate.convertAndSend(inceptionMachineTopic.getTopic(), new KillInceptionMachineMessage(rideId));
      }

      final StateMachineContext<States, Events> context = persister.read(machineId);
      if (context == null) {
        return Optional.empty();
      }
      resetMachine(rideId, inception, machine, context);
      return Optional.of(machine);
    } catch (Exception e) {
      log.error("Failed to restore machine", e);
      return Optional.empty();
    }
  }

  private void resetMachine(long rideId, boolean inception, StateMachine<States, Events> machine, StateMachineContext<States, Events> context) {
    machine.stop();
    machine.getStateMachineAccessor().doWithAllRegions(function -> function.resetStateMachine(context));
    machine.getExtendedState().getVariables().put("rideId", rideId);
    markInception(machine, inception);
    machine.start();
  }

  private StateMachine<States, Events> createMachine(boolean inception) {
    String beanName = "stateMachine";
    if (inception) {
      beanName = "inceptionStateMachine";
    }
    StateMachine<States, Events> machine = (StateMachine<States, Events>) beanFactory.getBean(beanName);
    machine.getExtendedState().getVariables().clear();
    markInception(machine, inception);
    return machine;
  }

  private void markInception(StateMachine<States, Events> machine, boolean inception) {
    machine.getExtendedState().getVariables().put("_inception", inception);
  }

  private Optional<StateMachine<States, Events>> forceRestore(long rideId, Events event, MessageHeaders headers) {
    Ride ride = rideDslRepository.findOne(rideId);
    if (ride == null) {
      return Optional.empty();
    }
    StateMachine<States, Events> machine = createMachine(ride, true);
    ActiveDriver activeDriver = ride.getActiveDriver();
    DispatchType dispatchType = DispatchType.REGULAR;
    if (areaService.isInArea(new LatLng(ride.getStartLocationLat(), ride.getStartLocationLong()), ride.getCityId()) != null) {
      dispatchType = DispatchType.QUEUED;
    }
    DispatchContext dispatchContext = new DispatchContext(rideId, ride.getCityId(), ride.getStartLocationLat(),
      ride.getStartLocationLong(), dispatchType);
    RideFlowContext flowContext = new RideFlowContext();
    if (activeDriver != null) {
      dispatchContext.setAccepted(true);
      DispatchCandidate candidate = rideDslRepository.findDispatchCandidate(ride.getId());
      dispatchContext.setCandidate(candidate);
      if (candidate != null) {
        flowContext.setDriver(candidate.getId());
      }
    }
    flowContext.setAcceptedOn(ride.getDriverAcceptedOn());
    flowContext.setReachedOn(ride.getDriverReachedOn());
    flowContext.setStartedOn(ride.getStartedOn());
    machine.getExtendedState().getVariables().put("dispatchContext", dispatchContext);
    machine.getExtendedState().getVariables().put("flowContext", flowContext);
    DefaultStateMachineContext<States, Events> context = new DefaultStateMachineContext<>(
      States.MAPPING.get(ride.getStatus()), event, headers, machine.getExtendedState()
    );
    resetMachine(rideId, event.causesInception(), machine, context);
    return Optional.of(machine);
  }
}
