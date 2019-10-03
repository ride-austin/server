package com.rideaustin.rest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessageDto {

  public static final String REASON = "REASON";
  public static final String REASON_KEY = "REASON-KEY";

  public enum ReasonKey {
    SESSION_TOKEN_INVALID,
    SESSION_EXPIRED,
    LOGGED_OUT,
    LOGGED_IN_ON_OTHER_DEVICE,
    INVALID_SESSION_TOKEN,
    WRONG_CREDENTIALS,
    CREDENTIALS_NOT_PROVIDED,
    UNKNOWN,
    SESSION_FOR_APP_TYPE_NOT_ENDED,
    USER_DISABLED
  }

  private String message;
  private String errorCode;
  private ReasonKey reasonKey;

}