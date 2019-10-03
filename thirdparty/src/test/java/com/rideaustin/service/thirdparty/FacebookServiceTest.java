package com.rideaustin.service.thirdparty;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import com.rideaustin.service.model.FacebookUser;

@RunWith(MockitoJUnitRunner.class)
public class FacebookServiceTest {

  @Mock
  private RestTemplateFactory restTemplateFactory;
  @Mock
  private RestTemplate restTemplate;
  private FacebookService facebookService;

  @Before
  public void init() {
    when(restTemplateFactory.get()).thenReturn(restTemplate);
    facebookService = new FacebookService(restTemplateFactory);
  }

  @Test
  public void shouldSetProperEmail() {
    // given
    FacebookUser facebookUser = FacebookUser.builder().email("stella_ja\\u0040live.com").build();
    when(restTemplate.getForObject(any(), eq(FacebookUser.class))).thenReturn(facebookUser);

    // when
    FacebookUser result = facebookService.getFacebookUser("any token");

    // then
    assertEquals("stella_ja@live.com", result.getEmail());
  }
}