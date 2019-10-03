package com.rideaustin.service.notifications.applications;

import org.springframework.stereotype.Component;

import com.rideaustin.filter.ClientAgentCity;
import com.rideaustin.model.Token;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.service.notifications.ApplicationPolicy;

@Component
public class AustinDriverIosApplicationPolicy implements ApplicationPolicy {
  @Override
  public boolean shouldUseApplication(Token token) {
    return token.getAgent().equals(ClientAgentCity.AUSTIN) &&
      token.getAvatarType().equals(AvatarType.DRIVER) &&
      token.getType().equals(Token.TokenType.APPLE);
  }
}
