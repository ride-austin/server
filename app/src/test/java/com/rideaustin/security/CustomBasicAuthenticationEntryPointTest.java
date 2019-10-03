package com.rideaustin.security;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;

public class CustomBasicAuthenticationEntryPointTest {

  private static final String REALM = "REALM";
  @Mock
  private Environment environment;
  @Mock
  private HttpServletResponse response;

  private CustomBasicAuthenticationEntryPoint testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(environment.getProperty("default.security.realm")).thenReturn(REALM);
    testedInstance = new CustomBasicAuthenticationEntryPoint(environment);
  }

  @Test
  public void commenceSetsHeader() throws IOException {
    testedInstance.commence(null, response, new BadCredentialsException("Bad credentials"));

    verify(response, times(1)).addHeader(eq(HttpHeaders.WWW_AUTHENTICATE), eq(String.format("Basic realm=\"%s\"", REALM)));
  }

  @Test
  public void commenceSendsError() throws IOException {
    final String message = "Bad credentials";
    testedInstance.commence(null, response, new BadCredentialsException(message));

    verify(response, times(1)).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), eq(message));
  }
}