package com.rideaustin.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.google.common.collect.Sets;

public class ClientAppVersionFilter extends GenericFilterBean {

  private static final String DRIVER_APP_VERSION_ERROR =
    "A new version of the app is now available. Please install the latest version. \n ";

  private static final String RIDER_APP_VERSION_ERROR =
    "A new version of the app is now available. Please get the latest version from App Store. \n ";

  private final ClientAppVersionFactory clientAppVersionFactory = new ClientAppVersionFactory();

  private Set<String> clients;

  private static final ThreadLocal<String> DRIVER_ERROR = new ThreadLocal<>();

  @Override
  protected void initFilterBean() {
    ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
    Environment environment = ctx.getBean(Environment.class);
    clients = Sets.newHashSet(environment.getProperty("reject.clients", "").split(","));
    String newAppUrl = environment.getProperty("reject.newapp_url", "");
    DRIVER_ERROR.set(DRIVER_APP_VERSION_ERROR + newAppUrl);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    ClientAppVersion appVersion = clientAppVersionFactory.createClientAppVersion((HttpServletRequest) request);
    if (clients.contains(appVersion.getUserAgent())) {
      blockConfiguredAppVersions((HttpServletResponse) response, appVersion);
    } else {
      ClientAppVersionContext.setClientAppVersion(appVersion);
      chain.doFilter(request, response);
      ClientAppVersionContext.clearClientAppVersion();
    }
  }

  private void blockConfiguredAppVersions(HttpServletResponse response, ClientAppVersion appVersion) throws IOException {
    response.setStatus(HttpStatus.SC_BAD_REQUEST);
    if (appVersion.getUserAgent().toUpperCase().contains("DRIVER")) {
      response.getOutputStream().write(DRIVER_ERROR.get().getBytes(StandardCharsets.UTF_8));
    } else {
      response.getOutputStream().write(RIDER_APP_VERSION_ERROR.getBytes(StandardCharsets.UTF_8));
    }
    response.flushBuffer();
  }
}
