package com.rideaustin.service;

import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.SessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CurrentSessionService {

  private final SessionRepository sessionRepository;
  private final SessionCache sessionCache;

  @Transactional
  public Session getCurrentSession(long userId) {
    List<Session> activeSessions = sessionRepository.findActiveSessionsByUser(userId);
    Session currentSession = null;
    if (!activeSessions.isEmpty()) {
      currentSession = activeSessions.get(0);
      removeDuplicateSession(activeSessions);
    }
    return currentSession;
  }

  @Transactional
  public Session getCurrentSession(User user) {
    return getCurrentSession(user.getId());
  }

  @Transactional
  public void refreshUserSession(User user, ApiClientAppType apiClientAppType) {
    if (user != null) {
      List<Session> activeSessions = sessionRepository.findActiveSessionsByUser(user.getId());
      if (!activeSessions.isEmpty()) {
        sessionCache.store(activeSessions.get(0), apiClientAppType);
        removeDuplicateSession(activeSessions);
      }
    }
  }

  private void removeDuplicateSession(List<Session> activeSessions) {
    //some old session may not have apiClientAppType
    activeSessions.forEach(ac -> {
      if (ac.getApiClientAppType() == null) {
        ac.setApiClientAppType(ApiClientAppType.getSuggestedAvatarTypeFromHeader(ac.getUserAgent()));
      }
    });
    Map<ApiClientAppType, List<Session>> sessionPerApiClientType = activeSessions.stream().collect(groupingBy(Session::getApiClientAppType));

    sessionPerApiClientType.values().forEach(sessions -> {
      if (sessions.size() > 1) {
        sessions.remove(0);
        sessions.forEach(session -> {
          session.setDeleted(true);
          sessionRepository.saveAndFlush(session);
        });
      }
    });
  }
}
