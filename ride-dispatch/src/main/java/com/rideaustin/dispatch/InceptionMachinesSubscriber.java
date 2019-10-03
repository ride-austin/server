package com.rideaustin.dispatch;

import org.springframework.messaging.support.GenericMessage;

public class InceptionMachinesSubscriber {

  private final InceptionMachinesTracker tracker;

  public InceptionMachinesSubscriber(InceptionMachinesTracker tracker) {
    this.tracker = tracker;
  }

  public void handleMessage(KillInceptionMachineMessage message) {
    this.tracker.stopMachines(message.getRideId());
  }

  public void handleMessage(ProxyEventMessage message) {
    this.tracker.proxyEvent(message.getRideId(), new GenericMessage<>(message.getEvent(), message.getHeaders()));
  }
}
