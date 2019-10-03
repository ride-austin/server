package com.rideaustin.service.thirdparty;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ServerError;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.RateLimitException;

public class StripeServiceImplTest {

  private StripeServiceImpl testedInstance;

  @Mock
  private Environment environment;
  @Mock
  private ConfigurationItemCache configurationItemCache;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(environment.getProperty(anyString())).thenReturn("");
    when(configurationItemCache.getConfigAsInt(any(), anyString(), anyString())).thenReturn(100);
    testedInstance = new StripeServiceImpl(environment, configurationItemCache);
  }

  @Test
  public void makeStripeServiceRequestWithRetries() throws Exception {
    expectedException.expect(APIConnectionException.class);

    testedInstance.makeStripeServiceRequestWithRetries(() -> {
      throw new APIConnectionException("");
    });
  }

  @Test
  public void testMakeStripeRequestHandleAPIConnectionException() throws Exception {
    expectedException.expect(ServerError.class);
    expectedException.expectMessage("Our service is temporarily unavailable, please try it again in few minutes.");

    testedInstance.makeStripeServiceRequest(() -> {
      throw new APIConnectionException("");
    });
  }

  @Test
  public void testMakeStripeRequestHandleRateLimitException() throws Exception {
    expectedException.expect(ServerError.class);
    expectedException.expectMessage("Our service is temporarily unavailable, please try it again in few minutes.");

    testedInstance.makeStripeServiceRequest(() -> {
      throw new RateLimitException("", "", "", 0, null);
    });
  }

  @Test
  public void testMakeStripeRequestHandleCardException() throws Exception {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Sorry, your credit card was not approved");

    testedInstance.makeStripeServiceRequest(() -> {
      throw new CardException("", "", "", "", "", "", 0, null);
    });
  }

  @Test
  public void testMakeStripeRequestHandleInvalidRequestException() throws Exception {
    final String message = "Message";
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage(message);

    testedInstance.makeStripeServiceRequest(() -> {
      throw new InvalidRequestException(message, "", "", 0, null);
    });
  }

  @Test
  public void testMakeStripeRequestHandleAuthenticationException() throws Exception {
    expectedException.expect(ServerError.class);

    testedInstance.makeStripeServiceRequest(() -> {
      throw new AuthenticationException("", "", 0);
    });
  }

  @Test
  public void testMakeStripeRequestHandleStripeException() throws Exception {
    expectedException.expect(ServerError.class);

    testedInstance.makeStripeServiceRequest(() -> {
      throw new APIException("", "", 0, null);
    });
  }

}