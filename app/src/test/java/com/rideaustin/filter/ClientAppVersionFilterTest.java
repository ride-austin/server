package com.rideaustin.filter;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.WebApplicationContext;

public class ClientAppVersionFilterTest {

  private static final String REJECTED_DRIVER_CLIENT = "DRIVER_A";
  private static final String REJECTED_RIDER_CLIENT = "RIDER_A";
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain chain;
  @Mock
  private ServletContext servletContext;
  @Mock
  private WebApplicationContext applicationContext;
  @Mock
  private Environment environment;
  @Mock
  private ServletOutputStream outputStream;

  private ClientAppVersionFilter testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ClientAppVersionFilter();
    testedInstance.setServletContext(servletContext);

    when(servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)).thenReturn(applicationContext);
    when(applicationContext.getBean(Environment.class)).thenReturn(environment);
    when(environment.getProperty(eq("reject.clients"), eq(""))).thenReturn(String.join(",", REJECTED_DRIVER_CLIENT, REJECTED_RIDER_CLIENT));
    when(response.getOutputStream()).thenReturn(outputStream);
  }

  @Test
  public void doFilterRejectsDriverClient() throws IOException, ServletException {
    when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(REJECTED_DRIVER_CLIENT);
    testedInstance.initFilterBean();

    testedInstance.doFilter(request, response, chain);

    verify(chain, never()).doFilter(request, response);
    verify(outputStream).write(argThat(new BaseMatcher<byte[]>() {
      @Override
      public boolean matches(Object o) {
        final String message = new String(((byte[]) o));
        return message.contains("A new version of the app is now available. Please install the latest version.");
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }

  @Test
  public void doFilterRejectsRiderClient() throws IOException, ServletException {
    when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn(REJECTED_RIDER_CLIENT);
    testedInstance.initFilterBean();

    testedInstance.doFilter(request, response, chain);

    verify(chain, never()).doFilter(request, response);
    verify(outputStream).write(argThat(new BaseMatcher<byte[]>() {
      @Override
      public boolean matches(Object o) {
        final String message = new String(((byte[]) o));
        return message.contains("A new version of the app is now available. Please get the latest version from App Store.");
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }

  @Test
  public void doFilterAcceptsNotRejectedClient() throws IOException, ServletException {
    when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn("RIDER_B");
    testedInstance.initFilterBean();

    testedInstance.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }
}