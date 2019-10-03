package com.rideaustin.rest.model;

import org.quartz.Trigger.TriggerState;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class JobDto {

  private final String name;

  private final String nextFireTime;

  private final TriggerState state;

}
