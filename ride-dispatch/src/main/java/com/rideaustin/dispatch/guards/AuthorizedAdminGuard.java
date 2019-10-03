package com.rideaustin.dispatch.guards;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.model.user.User;
import com.rideaustin.service.CurrentUserService;

public class AuthorizedAdminGuard extends BaseAuthorizedGuard {

  @Inject
  private CurrentUserService currentUserService;

  @Override
  protected boolean doEvaluate(StateContext<States, Events> context) {
    User user = currentUserService.getUser();
    return user.isAdmin();
  }
}
