package com.rideaustin.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class SimpleCORSFilter implements Filter {

  @VisibleForTesting
  static final ImmutableSet<String> ORIGINS = ImmutableSet.of(
    // fill in allowed origins
      "https://allowed.origin"
  );

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletResponse response = (HttpServletResponse) res;
    HttpServletRequest request = (HttpServletRequest) req;

    //allow requests coming from the management console
    //(to be updated once the console will be deployed on a staging server)
    String origin = request.getHeader(HttpHeaders.ORIGIN);
    if (origin != null && ORIGINS.contains(origin)) {
      response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    }
    response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, PUT, GET, OPTIONS, DELETE, PATCH");
    response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
    response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "x-requested-with, content-type, X-Auth-Token, Authorization");
    response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    if ("OPTIONS".equals(request.getMethod())) {
      try {
        response.getWriter().print("OK");
        response.getWriter().flush();
      } catch (IOException e) {
        log.error("IO Error, " + e.getMessage(), e);
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  public void init(FilterConfig filterConfig) {
    log.trace("Init called");
  }

  public void destroy() {
    log.trace("Filter destroyed");
  }

}
