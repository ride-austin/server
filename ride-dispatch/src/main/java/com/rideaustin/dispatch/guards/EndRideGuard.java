package com.rideaustin.dispatch.guards;

import static com.rideaustin.dispatch.LogUtil.flowInfo;

import java.util.Date;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import com.rideaustin.dispatch.messages.EndRideMessage;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EndRideGuard implements Guard<States, Events> {
  @Override
  public boolean evaluate(StateContext<States, Events> context) {
    RideFlowContext flowContext = StateMachineUtils.getFlowContext(context);
    EndRideMessage contextMessage = new EndRideMessage(context.getMessageHeaders());
    Date completedOn = contextMessage.getCompletedOn();

    Date startedOn = flowContext.getStartedOn();
    if (completedOn == null || startedOn == null || completedOn.before(startedOn)) {
      flowInfo(log, StateMachineUtils.getRideId(context), "Aborting EndRideAction because startedOn is after completedOn");
      return false;
    }
    return true;
  }
}
