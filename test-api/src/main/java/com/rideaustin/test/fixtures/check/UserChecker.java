package com.rideaustin.test.fixtures.check;

import java.util.Optional;

import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;

public class UserChecker implements RecordChecker<User> {

  private final UserDslRepository userDslRepository;

  public UserChecker(UserDslRepository userDslRepository) {
    this.userDslRepository = userDslRepository;
  }

  @Override
  public Optional<User> getIfExists(User source) {
    return Optional.ofNullable(userDslRepository.findAnyByEmail(source.getEmail()));
  }
}
