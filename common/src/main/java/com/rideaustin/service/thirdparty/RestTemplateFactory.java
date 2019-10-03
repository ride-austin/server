package com.rideaustin.service.thirdparty;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateFactory {

  private final RestTemplate restTemplate = new RestTemplate();

  @Nonnull
  public RestTemplate get() {
    return restTemplate;
  }

  @Nonnull
  public RestTemplate create(String username, String password) {
    return new BasicAuthRestTemplate(username, password);
  }

  private static class BasicAuthRestTemplate extends RestTemplate {

    BasicAuthRestTemplate(String username, String password) {
      addAuthentication(username, password);
    }

    private void addAuthentication(String username, String password) {
      if (username == null) {
        return;
      }
      List<ClientHttpRequestInterceptor> interceptors =
        Collections.singletonList(new BasicAuthorizationInterceptor(username, password));
      setRequestFactory(new InterceptingClientHttpRequestFactory(getRequestFactory(), interceptors));
    }

    private static class BasicAuthorizationInterceptor implements ClientHttpRequestInterceptor {

      private final String username;
      private final String password;

      private BasicAuthorizationInterceptor(String username, String password) {
        this.username = username;
        this.password = password == null ? "" : password;
      }

      @Override
      public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
        throws IOException {
        String token = Base64.getEncoder().encodeToString((this.username + ":" + this.password).getBytes());
        request.getHeaders().add("Authorization", "Basic " + token);
        return execution.execute(request, body);
      }

    }

  }
}
