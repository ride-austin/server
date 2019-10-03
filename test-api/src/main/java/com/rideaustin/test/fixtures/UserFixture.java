package com.rideaustin.test.fixtures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.rideaustin.model.user.Avatar;
import com.rideaustin.model.user.Gender;
import com.rideaustin.model.user.User;

public class UserFixture extends AbstractFixture<User> {

  private final String password;
  private final int avatarBitmask;
  private String phoneNumber;
  private String email;
  private String firstName;
  private String lastName;
  private Set<Avatar> avatars = new HashSet<>();

  UserFixture(String phoneNumber, String email, String firstName, String lastName, String password, int avatarBitmask) {
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.password = password;
    this.avatarBitmask = avatarBitmask;
  }

  public String getEmail() {
    return email;
  }

  public static UserFixtureBuilder builder() {
    return new UserFixtureBuilder();
  }

  @Override
  protected User createObject() {
    return User.builder()
      .phoneNumber(phoneNumber)
      .email(email)
      .firstname(firstName)
      .lastname(lastName)
      .password(password)
      .gender(Gender.MALE)
      .userEnabled(true)
      .emailVerified(true)
      .avatarTypesBitmask(avatarBitmask)
      .avatars(new ArrayList<>(avatars))
      .build();
  }

  public void addAvatar(Avatar avatar) {
    this.avatars.clear();
    this.avatars.add(avatar);
  }

  public static class UserFixtureBuilder {
    private String phoneNumber;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private int avatarBitmask;

    public UserFixtureBuilder phoneNumber(String phoneNumber) {
      this.phoneNumber = phoneNumber;
      return this;
    }

    public UserFixtureBuilder email(String email) {
      this.email = email;
      return this;
    }

    public UserFixtureBuilder firstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public UserFixtureBuilder lastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public UserFixtureBuilder password(String password) {
      this.password = password;
      return this;
    }

    public UserFixtureBuilder avatarBitmask(int bitmask) {
      this.avatarBitmask = bitmask;
      return this;
    }

    public UserFixture build() {
      return new UserFixture(phoneNumber, email, firstName, lastName, password, avatarBitmask);
    }

  }
}
