package com.rideaustin.driverstatistic;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.ApplicationListener;

import com.rideaustin.events.RideAcceptedEvent;

public class RideAcceptedByDriverEventInterceptor implements ApplicationListener<RideAcceptedEvent> {

  private CountDownLatch latch = new CountDownLatch(1);
  private RideAcceptedEvent event;


  public void onApplicationEvent(RideAcceptedEvent event) {
    this.event = event;
    latch.countDown();
  }

  public void clear() {
    this.event = null;
  }

  public RideAcceptedEvent getLastEvent() {
    return event;
  }

  public CountDownLatch getLatch() {
    return latch;
  }
}
