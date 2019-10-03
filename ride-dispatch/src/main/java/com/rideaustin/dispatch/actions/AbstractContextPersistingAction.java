package com.rideaustin.dispatch.actions;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

public abstract class AbstractContextPersistingAction implements Action<States, Events> {

  @Inject
  protected DefaultStateMachinePersister<States, Events, String> persister;
  @Inject
  protected Environment environment;

}
