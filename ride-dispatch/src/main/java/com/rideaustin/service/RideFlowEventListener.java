package com.rideaustin.service;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.rideaustin.events.AdminCancelledEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideFlowEventListener {

  private final RideFlowService rideFlowService;

  @EventListener
  public void handle(AdminCancelledEvent event) {
    rideFlowService.cancelAsAdmin(event.getRideId());
  }
}
