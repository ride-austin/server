package com.rideaustin.service.notifications;

import com.rideaustin.model.Token;

@FunctionalInterface
public interface SubscriptionPolicy {

  boolean shouldSubscribe(Token token);

}
