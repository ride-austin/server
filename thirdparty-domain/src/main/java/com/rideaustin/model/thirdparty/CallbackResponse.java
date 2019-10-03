package com.rideaustin.model.thirdparty;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CallbackResponse {

  private final String response;

  @JsonValue
  @JsonRawValue
  public String getResponse() {
    return response;
  }
}
