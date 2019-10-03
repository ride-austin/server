package com.rideaustin.service.notifications;

import com.rideaustin.model.Token;

@FunctionalInterface
public interface ApplicationPolicy {

  boolean shouldUseApplication(Token token);

}
