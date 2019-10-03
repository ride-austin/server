package com.rideaustin.test.fixtures;

import com.rideaustin.model.Session;

public class SessionFixture extends AbstractFixture<Session> {

  private final String userAgent;

  public SessionFixture(String userAgent) {
    this.userAgent = userAgent;
  }

  @Override
  protected Session createObject() {
    return Session.builder()
      .userAgent(userAgent)
      .build();
  }
}
