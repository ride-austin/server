package com.rideaustin.application.cache.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.service.SessionService.MessageSessionClosed;
import com.rideaustin.service.event.EventManager;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SessionClosedSubscriber {

  private final EventManager eventManager;

  public void handleMessage(MessageSessionClosed notification) {
    eventManager.unregisterDriver(notification.getUserId());
    eventManager.unregisterRider(notification.getUserId());
  }
}
