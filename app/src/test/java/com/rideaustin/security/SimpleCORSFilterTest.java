package com.rideaustin.security;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class SimpleCORSFilterTest {

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain filterChain;
  @Mock
  private PrintWriter printWriter;

  private SimpleCORSFilter testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new SimpleCORSFilter();
  }

  @DataProvider
  public static Object[] eligibleSources() {
    return SimpleCORSFilter.ORIGINS.toArray();
  }

  @Test
  @UseDataProvider("eligibleSources")
  public void filterSetsAllowOriginHeader(String source) throws IOException, ServletException {
    when(request.getHeader(eq(HttpHeaders.ORIGIN))).thenReturn(source);

    testedInstance.doFilter(request, response, filterChain);

    verify(response, times(1)).setHeader(eq(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), eq(source));
  }

  @Test
  public void filterSetsAllowHeaders() throws IOException, ServletException {
    testedInstance.doFilter(request, response, filterChain);

    verify(response, times(1)).setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, PUT, GET, OPTIONS, DELETE, PATCH");
    verify(response, times(1)).setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
    verify(response, times(1)).setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "x-requested-with, content-type, X-Auth-Token, Authorization");
    verify(response, times(1)).setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
  }

  @Test
  public void filterFlushesOptionsRequest() throws IOException, ServletException {
    when(request.getMethod()).thenReturn(HttpMethod.OPTIONS.name());
    when(response.getWriter()).thenReturn(printWriter);

    testedInstance.doFilter(request, response, filterChain);

    verify(printWriter, times(1)).flush();
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  public void filterPassesNonOptionsRequestFurtherToChain() throws IOException, ServletException {
    when(request.getMethod()).thenReturn(HttpMethod.GET.name());
    when(response.getWriter()).thenReturn(printWriter);

    testedInstance.doFilter(request, response, filterChain);

    verify(printWriter, never()).flush();
    verify(filterChain, times(1)).doFilter(request, response);
  }
}