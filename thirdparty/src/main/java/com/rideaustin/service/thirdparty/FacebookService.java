package com.rideaustin.service.thirdparty;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.rideaustin.service.model.FacebookUser;

@Component
public class FacebookService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacebookService.class);
  private static final String FACEBOOK_URL = "https://graph.facebook.com/";

  private RestTemplate restTemplate;

  @Inject
  public FacebookService(RestTemplateFactory restTemplateFactory) {
    this.restTemplate = restTemplateFactory.get();
  }

  @Nullable
  public FacebookUser getFacebookUser(@Nonnull String fbAccessToken) {
    try {
      URI uri = new URIBuilder(FACEBOOK_URL + "me")
        .addParameter("access_token", fbAccessToken)
        .addParameter("format", "json")
        .addParameter("fields", "id,email").build();

      FacebookUser facebookUser = restTemplate.getForObject(uri, FacebookUser.class);
      if (facebookUser.getEmail() != null) {
        if (facebookUser.getEmail().contains("\\u0040")) {
          facebookUser.setEmail(facebookUser.getEmail().replace("\\u0040", "@"));
        }
        return facebookUser;
      }
    } catch (URISyntaxException e) {
      LOGGER.error("Failed to load facebook user for access token: {}", fbAccessToken, e);
    }
    return null;

  }

  public String getPhotoUrl(FacebookUser fbUser) {
    return FACEBOOK_URL + fbUser.getId() + "/picture?type=large";
  }
}
