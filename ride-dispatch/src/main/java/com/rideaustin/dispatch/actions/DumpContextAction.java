package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.flowInfo;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.rideaustin.application.cache.impl.JedisClient;
import com.rideaustin.dispatch.KillInceptionMachineMessage;
import com.rideaustin.dispatch.LogUtil;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.SessionDslRepository;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DumpContextAction implements Action<States, Events> {

  @Inject
  private RideDslRepository rideRepository;
  @Inject
  private SessionDslRepository sessionDslRepository;
  @Inject
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Inject
  private Environment environment;
  @Inject
  private JedisClient jedisClient;
  @Inject
  @Named("rideFlowRedisTemplate")
  private RedisTemplate redisTemplate;
  @Inject
  @Named("inceptionMachinesTopic")
  private ChannelTopic inceptionMachinesTopic;

  @Override
  public void execute(StateContext<States, Events> context) {
    Long rideId = StateMachineUtils.getRideId(context);
    RideFlowContext flowContext = StateMachineUtils.getFlowContext(context);
    Ride ride = rideRepository.findOne(rideId);
    if (ride == null) {
      flowInfo(log, rideId, "Ride not found");
      return;
    }
    Optional<RideFlowContext> optionalContext = Optional.of(flowContext);
    optionalContext.map(RideFlowContext::getStartedOn).ifPresent(ride::setStartedOn);
    optionalContext.map(RideFlowContext::getAcceptedOn).ifPresent(ride::setDriverAcceptedOn);
    optionalContext.map(RideFlowContext::getReachedOn).ifPresent(ride::setDriverReachedOn);
    if (flowContext.getDriverSession() != null) {
      ride.setDriverSession(sessionDslRepository.findOne(flowContext.getDriverSession()));
      if (ride.getActiveDriver() == null) {
        LogUtil.flowInfo(log, rideId,
          String.format("[DUMPCONTEXT] Cancel and accept collision, assure that driver %d is set", flowContext.getDriver()));
        ride.setActiveDriver(activeDriverDslRepository.findById(flowContext.getDriver()));
      }
    }
    rideRepository.save(ride);
    flowInfo(log, StateMachineUtils.getRequestContext(context), "Dumped flow context to db");
    jedisClient.remove(StateMachineUtils.getMachineId(environment, context.getStateMachine()));
    flowInfo(log, StateMachineUtils.getRequestContext(context), "Removed ride context from cache");
    redisTemplate.convertAndSend(inceptionMachinesTopic.getTopic(), new KillInceptionMachineMessage(rideId));
    flowInfo(log, StateMachineUtils.getRequestContext(context), "Removed ride preauth from cache");
    jedisClient.remove(String.format("ride:%d:preauth", rideId));
  }
}
