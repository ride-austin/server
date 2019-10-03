package com.rideaustin.service.notifications.applications;

import org.springframework.stereotype.Component;

import com.rideaustin.model.Token;
import com.rideaustin.service.notifications.ApplicationPolicy;

@Component
public class AndroidGenericApplicationPolicy implements ApplicationPolicy {
  @Override
  public boolean shouldUseApplication(Token token) {
    return
      token.getType().equals(Token.TokenType.GOOGLE);
  }
}
