package com.rideaustin.service.security;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.enums.SessionClosingReason;
import com.rideaustin.rest.model.ErrorMessageDto;
import com.rideaustin.service.SessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BasicAndTokenAuthenticationService {

  static final String AUTHENTICATION_ERROR_MESSAGE = "Authentication error";
  static final String LOGGED_IN_OTHER_DEVICE_MESSAGE = "Login invalid because you are already logged in with another device. Please log into one device only.";
  static final String LOGGED_IN_OTHER_DEVICE_FORMAT_MESSAGE = "Login invalid because user is driving using other device (%s)";
  static final String LOGGED_IN_OTHER_DEVICE_NO_FORMAT_MESSAGE = "Login invalid because user is driving using other device";

  private final SessionService sessionService;

  @Transactional
  public void handleTokenInvalidReason(String token, ApiClientAppType apiClientAppType, HttpServletRequest request) {
    Session deletedSession = sessionService.findDeletedByAuthTokenWithUser(token);
    if (null == deletedSession) {
      storeReason(AUTHENTICATION_ERROR_MESSAGE, ErrorMessageDto.ReasonKey.INVALID_SESSION_TOKEN, request);
    } else {
      Session currentUserSession = sessionService.findCurrentSessionByUserEmailAndAppTypeWithUser(
        deletedSession.getUser().getEmail(), apiClientAppType);
      if (null == currentUserSession) {
        if (SessionClosingReason.LOGOUT.equals(deletedSession.getSessionClosingReason())) {
          storeReason(AUTHENTICATION_ERROR_MESSAGE, ErrorMessageDto.ReasonKey.LOGGED_OUT, request);
        } else if (SessionClosingReason.SESSION_EXPIRE.equals(deletedSession.getSessionClosingReason())) {
          storeReason(AUTHENTICATION_ERROR_MESSAGE, ErrorMessageDto.ReasonKey.SESSION_EXPIRED, request);
        } else {
          storeReason(AUTHENTICATION_ERROR_MESSAGE, ErrorMessageDto.ReasonKey.INVALID_SESSION_TOKEN, request);
        }
      } else if (apiClientAppType != ApiClientAppType.OTHER) {
        storeReason(LOGGED_IN_OTHER_DEVICE_MESSAGE, ErrorMessageDto.ReasonKey.LOGGED_IN_ON_OTHER_DEVICE, request);
      }
    }
  }

  public void handleLoggedOnOtherDevice(HttpServletRequest request, Session session) {
    if (session.getUserDevice() != null) {
      storeReason(String.format(LOGGED_IN_OTHER_DEVICE_FORMAT_MESSAGE, session.getUserDevice()), ErrorMessageDto.ReasonKey.LOGGED_IN_ON_OTHER_DEVICE, request);
    } else {
      storeReason(LOGGED_IN_OTHER_DEVICE_NO_FORMAT_MESSAGE, ErrorMessageDto.ReasonKey.LOGGED_IN_ON_OTHER_DEVICE, request);
    }
  }

  public void storeReason(String reason, ErrorMessageDto.ReasonKey reasonKey, HttpServletRequest request) {
    request.setAttribute(ErrorMessageDto.REASON, reason);
    request.setAttribute(ErrorMessageDto.REASON_KEY, reasonKey);
  }

  public static String[] decodeBasicAuthKey(final String auth) {
    try {
      if (auth != null) {
        //Replacing "Basic THE_BASE_64" to "THE_BASE_64" directly
        String encodedAuthorization = auth.replaceFirst("[B|b]asic ", "");
        //Decode the Base64 into byte[]
        byte[] decodedBytes = DatatypeConverter.parseBase64Binary(encodedAuthorization);

        //If the decode fails in any case
        if (decodedBytes == null || decodedBytes.length == 0) {
          return new String[0];
        }

        //Now we can convert the byte[] into a split array :
        //  - the first one is login,
        //  - the second one password
        return new String(decodedBytes).split(":", 2);
      }
    } catch (Exception e) {
      log.error("Problems finding authorization from request", e);
    }
    return new String[0];
  }

  public Session handleUserAgentChange(Session session, ApiClientAppType apiClientAppType, String userAgent) {
    Session replacedSession = session;
    if (apiClientAppType.isMobileApp() && apiClientAppType.equals(session.getApiClientAppType()) && !session.getUserAgent().equals(userAgent)) {
      String authToken = session.getAuthToken();
      sessionService.expirePreviousSession(session, SessionClosingReason.AGENT_CHANGE);
      replacedSession = sessionService.replaceSession(session, userAgent, apiClientAppType, authToken);
    }
    return replacedSession;
  }
}
