package com.rideaustin.clients.configuration;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.ApplicationListener;

import com.rideaustin.clients.configuration.events.ConfigurationItemChangedEvent;

public class ConfigurationItemChangedEventInterceptor implements ApplicationListener<ConfigurationItemChangedEvent> {


  private CountDownLatch latch = new CountDownLatch(1);
  private ConfigurationItemChangedEvent event;


  public void onApplicationEvent(ConfigurationItemChangedEvent event) {
    this.event = event;
    latch.countDown();
  }

  public void clear() {
    this.event = null;
  }

  public ConfigurationItemChangedEvent getLastEvent() {
    return event;
  }

  public CountDownLatch getLatch() {
    return latch;
  }
}
