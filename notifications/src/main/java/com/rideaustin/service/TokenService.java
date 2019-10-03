package com.rideaustin.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.Token;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.TokenRepository;
import com.rideaustin.service.notifications.PushNotificationsFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TokenService {

  private final TokenRepository tokenRepository;

  private final PushNotificationsFacade pushNotificationsFacade;

  @Transactional
  public void deleteTokensAndTopicSubscriptions(User user, AvatarType avatarType) {
    List<Token> tokens = tokenRepository.findByUserAndAvatarType(user, avatarType);
    for (Token token : tokens) {
      pushNotificationsFacade.unsubscribeFromTopics(token);
    }

    tokenRepository.delete(tokens);
  }
}
