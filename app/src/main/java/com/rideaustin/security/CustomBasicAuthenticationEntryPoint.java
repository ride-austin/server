package com.rideaustin.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Getter(AccessLevel.PROTECTED)
  @Setter(AccessLevel.PROTECTED)
  private String defaultRealmName;

  public CustomBasicAuthenticationEntryPoint(Environment env) {
    this.setDefaultRealmName(env.getProperty("default.security.realm"));
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
    AuthenticationException authException) throws IOException {
    response.addHeader(HttpHeaders.WWW_AUTHENTICATE, String.format("Basic realm=\"%s\"", this.getDefaultRealmName()));
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
  }

}
