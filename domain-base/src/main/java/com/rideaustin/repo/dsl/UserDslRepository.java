package com.rideaustin.repo.dsl;

import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.user.QUser;
import com.rideaustin.model.user.User;

@Repository
public class UserDslRepository extends AbstractDslRepository {

  private static final QUser qUser = QUser.user;

  public void changePassword(@Nonnull Long userId, @Nonnull String newEncodedPassword) {
    queryFactory.update(qUser)
      .where(qUser.id.eq(userId))
      .set(qUser.password, newEncodedPassword)
      .execute();
  }

  public boolean isPhoneNumberInUse(String phoneNumber) {
    return buildQuery(qUser)
      .where(
        qUser.phoneNumber.eq(phoneNumber),
        qUser.userEnabled.isTrue()
      )
      .fetchCount() > 0;
  }

  public List<User> findAnyByPhoneNumber(String phoneNumber) {
    return buildQuery(qUser)
      .where(
        qUser.phoneNumber.eq(phoneNumber)
      )
      .fetch();
  }

  public List<User> findByPhoneNumber(String phoneNumber) {
    return buildQuery(qUser)
      .where(
        qUser.phoneNumber.eq(phoneNumber),
        qUser.userEnabled.isTrue()
      )
      .fetch();
  }

  public User findAnyByEmail(String email) {
    return buildQuery(qUser)
      .where(
        qUser.email.eq(email)
      )
      .fetchOne();
  }

  public User findByEmail(String email) {
    return buildQuery(qUser)
      .where(
        qUser.email.eq(email),
        qUser.userEnabled.isTrue()
      )
      .fetchOne();
  }

  public User getWithDependencies(Long userId) {
    return buildQuery(qUser)
      .leftJoin(qUser.avatars).fetchJoin()
      .where(qUser.id.eq(userId))
      .fetchOne();
  }

  public User findOne(long userId) {
    return get(userId, User.class);
  }

}
