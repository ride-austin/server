package com.rideaustin.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.SessionClosingReason;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.BlockedDeviceDslRepository;
import com.rideaustin.repo.dsl.SessionDslRepository;
import com.rideaustin.repo.jpa.SessionRepository;
import com.rideaustin.service.user.BlockedDeviceRegistry;
import com.rideaustin.util.SessionUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SessionService {

  private static final String USER_AGENT = "User-Agent";

  private final SessionRepository sessionRepository;
  private final SessionCache sessionCache;
  private final RedisTemplate redisTemplate;
  private final ChannelTopic sessionClosedChannel;
  private final SessionDslRepository sessionDslRepository;
  private final BlockedDeviceDslRepository blockedDeviceDslRepository;
  private final BlockedDeviceRegistry blockedDeviceRegistry;
  private final TokenService tokenService;

  @Inject
  public SessionService(SessionRepository sessionRepository, SessionCache sessionCache,
    @Named("rideFlowRedisTemplate") RedisTemplate redisTemplate,
    @Named("sessionClosedTopic") ChannelTopic sessionClosedChannel,
    SessionDslRepository sessionDslRepository, BlockedDeviceDslRepository blockedDeviceDslRepository,
    BlockedDeviceRegistry blockedDeviceRegistry, TokenService tokenService) {
    this.sessionRepository = sessionRepository;
    this.sessionCache = sessionCache;
    this.redisTemplate = redisTemplate;
    this.sessionClosedChannel = sessionClosedChannel;
    this.sessionDslRepository = sessionDslRepository;
    this.blockedDeviceDslRepository = blockedDeviceDslRepository;
    this.blockedDeviceRegistry = blockedDeviceRegistry;
    this.tokenService = tokenService;
  }

  @Transactional
  public Session createNewSession(HttpServletRequest request, User user) {
    ApiClientAppType apiClientAppType = ApiClientAppType.getSuggestedAvatarTypeFromHeader(request.getHeader(USER_AGENT));
    endSession(user, apiClientAppType, SessionClosingReason.NEW_SESSION_START);
    Session session = new Session();
    session.setUser(user.deepCopy(user));
    session.setUserAgent(request.getHeader(USER_AGENT));
    session.setUserPlatform(request.getHeader("User-Platform"));
    session.setUserDevice(request.getHeader("User-Device"));
    session.setUserDeviceId(request.getHeader("User-Device-Id"));
    session.setUserDeviceOther(request.getHeader("User-Device-Other"));
    session.setVersion(request.getHeader("Accept-Version"));
    session.setApiClientAppType(apiClientAppType);
    session.setAuthToken(AuthTokenUtils.generateAuthToken(user));
    session.setExpiresOn(AuthTokenUtils.calculateTokenExpirationTime(SessionUtils.isLoginRequestFromMobile(request.getHeader(USER_AGENT))));
    session = sessionRepository.saveAndFlush(session);

    sessionCache.store(session, session.getApiClientAppType());
    return session;
  }

  /**
   * This method is used to replace the user session.
   * Primarily it was designed to replace session when user upgrades/changes client without relogin.
   *
   * @param session
   * @param userAgent
   * @param apiClientAppType
   * @param authToken
   * @return
   */
  @Transactional
  public Session replaceSession(Session session, String userAgent, ApiClientAppType apiClientAppType, final String authToken) {
    Session newSession = new Session();
    newSession.setUser(session.getUser());
    newSession.setUserAgent(userAgent);
    newSession.setUserPlatform(session.getUserPlatform());
    newSession.setUserDevice(session.getUserDevice());
    newSession.setUserDeviceId(session.getUserDeviceId());
    newSession.setUserDeviceOther(session.getUserDeviceOther());
    newSession.setVersion(session.getVersion());
    newSession.setApiClientAppType(apiClientAppType);
    newSession.setAuthToken(authToken);
    newSession.setExpiresOn(AuthTokenUtils.calculateTokenExpirationTime(SessionUtils.isLoginRequestFromMobile(userAgent)));
    newSession = sessionDslRepository.save(newSession);
    sessionCache.store(newSession, apiClientAppType);
    return newSession;
  }

  @Transactional
  public void expirePreviousSession(Session session, SessionClosingReason closingReason) {
    String authToken = session.getAuthToken();
    session.setDeleted(true);
    session.setSessionClosingReason(closingReason);
    session.setAuthToken(authToken + session.getId());
    sessionDslRepository.save(session);
  }

  @Transactional
  public void endSessionsImmediately(User user) {
    List<Session> activeSessions = sessionDslRepository.findCurrentSessionsByUser(user.getId());
    activeSessions.forEach(session -> {
      session.setDeleted(true);
      session.setSessionClosingReason(SessionClosingReason.ADMIN_DISABLE);
      sessionRepository.saveAndFlush(session);
      sessionCache.remove(session);
    });
    tokenService.deleteTokensAndTopicSubscriptions(user, AvatarType.DRIVER);
    tokenService.deleteTokensAndTopicSubscriptions(user, AvatarType.RIDER);
  }

  @Transactional
  public void endSession(User user, ApiClientAppType apiClientAppType, SessionClosingReason sessionClosingReason) {
    List<Session> activeSessions = sessionDslRepository.findAllActiveSessionByUserEmailAndAppTypeWithUser(user.getEmail(), apiClientAppType);
    activeSessions.forEach(session -> {
      session.setDeleted(true);
      session.setSessionClosingReason(sessionClosingReason);
      sessionRepository.saveAndFlush(session);
      sessionCache.remove(session);
    });

    if (apiClientAppType.equals(ApiClientAppType.MOBILE_RIDER)) {
      tokenService.deleteTokensAndTopicSubscriptions(user, AvatarType.RIDER);
    } else if (apiClientAppType.equals(ApiClientAppType.MOBILE_DRIVER)) {
      tokenService.deleteTokensAndTopicSubscriptions(user, AvatarType.DRIVER);
    }

    if (apiClientAppType.equals(ApiClientAppType.MOBILE_DRIVER) && !activeSessions.isEmpty()) {
      // broadcast session closed message
      // (to initiate closing of current long-poll request on some other server)
      publishSessionClosedMessage(user);
    }
  }

  @Transactional
  public Optional<Session> retrieveSession(String token, ApiClientAppType apiClientAppType) {
    String userId;
    if (token != null && token.contains(":")) {
      String[] parts = token.split(":");
      userId = parts[0];
    } else {
      return Optional.empty();
    }
    Session session = sessionCache.retrieve(userId, apiClientAppType);
    if (session != null && SessionUtils.checkToken(apiClientAppType, token, session.getAuthToken())) {
      return checkSessionBlock(session);
    } else {
      Optional<Session> dbSession = Optional.ofNullable(getSessionByTokenWithFallback(token, Long.valueOf(userId), apiClientAppType));
      if (dbSession.isPresent()) {
        dbSession = checkSessionBlock(dbSession.get());
      }
      dbSession.ifPresent(s -> sessionCache.store(s, apiClientAppType));
      return dbSession;
    }
  }

  @Transactional
  public Session getSessionByLoginWithFallback(String email, ApiClientAppType apiClientAppType) {
    Session session = sessionDslRepository.findCurrentSessionByUserEmailAndAppTypeWithUser(email, apiClientAppType);
    if (session == null) {
      session = sessionDslRepository.findByUserEmailAndNoClientTypeWithUser(email);
      if (session != null) {
        session.setApiClientAppType(apiClientAppType);
        session = sessionDslRepository.save(session);
      }
    }
    return session;
  }

  @Transactional
  public Session findDeletedByAuthTokenWithUser(String token) {
    return sessionDslRepository.findDeletedByAuthTokenWithUser(token);
  }

  @Transactional
  public Session findCurrentSessionByUserEmailAndAppTypeWithUser(String email, ApiClientAppType apiClientAppType) {
    return sessionDslRepository.findCurrentSessionByUserEmailAndAppTypeWithUser(email, apiClientAppType);
  }

  private Session getSessionByTokenWithFallback(String token, Long userId, ApiClientAppType apiClientAppType) {
    if (blockedDeviceDslRepository.isBlocked(userId)) {
      return null;
    }
    Session session = sessionDslRepository.findByAuthTokenAndClientWithUser(token, apiClientAppType);
    if (session == null) {
      session = sessionDslRepository.findByAuthTokenAndNoClientTypeWithUser(token);
      if (session != null) {
        session.setApiClientAppType(apiClientAppType);
        session = sessionDslRepository.save(session);
      }
    }
    if (session != null && session.getUserDeviceId() != null && blockedDeviceRegistry.isBlocked(session.getUserDeviceId())) {
      return null;
    }
    return session;
  }

  private Optional<Session> checkSessionBlock(Session session) {
    if (session.getUserDeviceId() != null && blockedDeviceRegistry.isBlocked(session.getUserDeviceId())) {
      return Optional.empty();
    }
    return Optional.of(session);
  }

  /**
   * Broadcast session closed message to all
   *
   * @param user
   */
  public void publishSessionClosedMessage(User user) {
    redisTemplate.convertAndSend(this.sessionClosedChannel.getTopic(), new MessageSessionClosed(user.getId()));
  }

  @NoArgsConstructor
  @AllArgsConstructor
  public static class MessageSessionClosed implements Serializable {

    private static final long serialVersionUID = 138168161916861438L;

    @Getter
    @Setter
    private Long userId;
  }

}
