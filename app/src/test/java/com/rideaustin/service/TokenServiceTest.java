package com.rideaustin.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.Token;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.TokenRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.notifications.PushNotificationsFacade;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceTest {

  @InjectMocks
  private TokenService tokenService;

  @Mock
  private TokenRepository tokenRepository;

  @Mock
  private PushNotificationsFacade pushNotificationsFacade;

  private User user = new User();

  @Test
  public void shouldDeleteTokens(){
    tokenService.deleteTokensAndTopicSubscriptions(user, AvatarType.RIDER);

    verify(tokenRepository, times(1)).findByUserAndAvatarType(any(), any());
    verify(tokenRepository, times(1)).delete(anyList());
  }

  @Test
  public void shouldDeleteTokensAndTopicSubscriptions_ForRider() throws ServerError {
    Token token = new Token();
    token.setTopicSubscriptions("topicSubscriptionArn");
    List<Token> tokens = Collections.singletonList(token);
    when(tokenRepository.findByUserAndAvatarType(any(), any())).thenReturn(tokens);

    tokenService.deleteTokensAndTopicSubscriptions(user, AvatarType.RIDER);

    verify(tokenRepository, times(1)).findByUserAndAvatarType(any(), any());
    verify(tokenRepository, times(1)).delete(anyList());
    verify(pushNotificationsFacade, times(1)).unsubscribeFromTopics(any());
  }

}
