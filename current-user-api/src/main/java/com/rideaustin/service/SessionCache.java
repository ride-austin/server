package com.rideaustin.service;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.repo.redis.SessionRedisRepository;
import com.rideaustin.util.SessionUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SessionCache {

  private final SessionRedisRepository sessionRedisRepository;

  public void store(Session session, ApiClientAppType apiClientAppType) {
    session.setUser(session.getUser().deepCopy(session.getUser()));
    String key = SessionUtils.buildSessionKey(session, apiClientAppType);
    session.setRedisKey(key);
    sessionRedisRepository.save(session);
  }

  public void remove(Session session) {
    sessionRedisRepository.delete(SessionUtils.buildSessionKey(session, session.getApiClientAppType()));
  }

  public Session retrieve(String userId, ApiClientAppType apiClientAppType) {
    return sessionRedisRepository.findOne(SessionUtils.buildSessionKey(userId, apiClientAppType));
  }
}
