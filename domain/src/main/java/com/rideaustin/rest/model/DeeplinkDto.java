package com.rideaustin.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeeplinkDto {

  private final String playLink;
  private final String appStoreLink;
  @JsonIgnore
  private final String token;

  @JsonProperty
  public String getDeeplink() {
    return String.format("rideaustin://requestToken=%s", token);
  }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public static DeeplinkDto fromJson(@JsonProperty("playLink") String playLink, @JsonProperty("appStoreLink") String appStoreLink,
    @JsonProperty("deeplink") String deeplink) {
    return new DeeplinkDto(playLink, appStoreLink, deeplink.replace("rideaustin://requestToken=", ""));
  }
}
