package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.QSession;
import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.user.QUser;
import com.rideaustin.model.user.User;

@Repository
public class SessionDslRepository extends AbstractDslRepository {

  private static final QSession qSession = QSession.session;
  private static final QUser qUser = QUser.user;

  public Session findByAuthTokenAndClientWithUser(String authToken, ApiClientAppType apiClientAppType) {
    return buildQuery(qSession)
      .join(qSession.user, qUser).fetchJoin()
      .where(
        qSession.authToken.eq(authToken),
        qSession.apiClientAppType.eq(apiClientAppType),
        qSession.deleted.isFalse()
      )
      .fetchOne();
  }

  public Session findByAuthTokenAndNoClientTypeWithUser(String authToken) {
    return buildQuery(qSession)
      .join(qSession.user, qUser).fetchJoin()
      .where(qSession.authToken.eq(authToken)
        .and(qSession.deleted.isFalse())
        .and(qSession.apiClientAppType.isNull()))
      .fetchOne();
  }

  public Session findDeletedByAuthTokenWithUser(String authToken) {
    return buildQuery(qSession)
      .join(qSession.user, qUser).fetchJoin()
      .where(qSession.authToken.eq(authToken).and(qSession.deleted.isTrue()))
      .fetchOne();
  }

  public Session findCurrentSessionByUserEmailAndAppTypeWithUser(String userEmail, ApiClientAppType apiClientAppType) {
    return buildQuery(qSession)
      .join(qSession.user, qUser).fetchJoin()
      .where(qUser.email.eq(userEmail)
        .and(qSession.apiClientAppType.eq(apiClientAppType))
        .and(qSession.deleted.isFalse()))
      .fetchOne();
  }

  public List<Session> findCurrentSessionsByUser(long userId) {
    return buildQuery(qSession)
      .join(qSession.user, qUser).fetchJoin()
      .where(qUser.id.eq(userId)
        .and(qSession.deleted.isFalse()))
      .fetch();
  }

  public List<Session> findAllActiveSessionByUserEmailAndAppTypeWithUser(String userEmail, ApiClientAppType apiClientAppType) {
    return buildQuery(qSession)
      .join(qSession.user, qUser).fetchJoin()
      .where(qUser.email.eq(userEmail)
        .and(qSession.apiClientAppType.eq(apiClientAppType))
        .and(qSession.deleted.isFalse()))
      .fetch();
  }

  public Session findByUserEmailAndNoClientTypeWithUser(String userEmail) {
    return buildQuery(qSession)
      .join(qSession.user, qUser).fetchJoin()
      .where(qUser.email.eq(userEmail)
        .and(qSession.apiClientAppType.isNull())
        .and(qSession.deleted.isFalse()))
      .fetchOne();
  }

  public Session findLastSession(User user) {
    return buildQuery(qSession)
      .where(
        qSession.user.eq(user),
        qSession.deleted.isTrue()
      )
      .limit(1)
      .fetchOne();
  }

  public Session findOne(long id) {
    return get(id, Session.class);
  }

  public List<String> findAllDeviceIds(User user) {
    return buildQuery(qSession)
      .where(qSession.user.eq(user))
      .select(qSession.userDeviceId)
      .distinct()
      .fetch();
  }
}
