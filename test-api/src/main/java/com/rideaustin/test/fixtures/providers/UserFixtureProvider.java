package com.rideaustin.test.fixtures.providers;

import javax.persistence.EntityManager;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.test.fixtures.UserFixture;
import com.rideaustin.test.fixtures.check.UserChecker;
import com.rideaustin.test.utils.RandomUtils;

public class UserFixtureProvider {

  private final EntityManager entityManager;

  private final PasswordEncoder passwordEncoder;

  private final UserDslRepository userDslRepository;

  public UserFixtureProvider(EntityManager entityManager, PasswordEncoder passwordEncoder, UserDslRepository userDslRepository) {
    this.entityManager = entityManager;
    this.passwordEncoder = passwordEncoder;
    this.userDslRepository = userDslRepository;
  }

  public UserFixture create() {
    return create(AvatarType.RIDER);
  }

  public UserFixture create(AvatarType... avatarTypes) {
    String randomEmail = RandomUtils.randomEmail();
    UserFixture userFixture = UserFixture.builder()
      .phoneNumber(RandomUtils.randomPhoneNumber())
      .email(randomEmail)
      .firstName(RandomUtils.randomName())
      .lastName(RandomUtils.randomName())
      .password(passwordEncoder.encode(randomEmail))
      .avatarBitmask(getBitMask(avatarTypes))
      .build();

    userFixture.setEntityManager(entityManager);
    userFixture.setRecordChecker(new UserChecker(userDslRepository));
    return userFixture;
  }

  private int getBitMask(AvatarType... avatarTypes) {
    int finalBitMask = 0;
    for (AvatarType avatarType : avatarTypes) {
      finalBitMask = finalBitMask | avatarType.toBitMask();
    }

    return finalBitMask;
  }
}
