package com.rideaustin.dispatch.messages;

import java.util.Date;

import org.springframework.messaging.MessageHeaders;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.RideEndLocation;

public class EndRideMessage extends DeferredResultMessage<MobileDriverRideDto> {

  private static final String END_LOCATION_KEY = "endLocation";
  private static final String COMPLETED_ON_KEY = "completedOn";

  public EndRideMessage(MessageHeaders messageHeaders) {
    super(messageHeaders);
  }

  public EndRideMessage(Date completedOn, RideEndLocation endLocation) {
    super(ImmutableMap.of(END_LOCATION_KEY, endLocation, COMPLETED_ON_KEY, completedOn));
  }

  public EndRideMessage(RideEndLocation endLocation, DeferredResult<MobileDriverRideDto> deferredResult) {
    super(deferredResult, ImmutableMap.of(END_LOCATION_KEY, endLocation, COMPLETED_ON_KEY, new Date()));
  }

  public RideEndLocation getEndLocation() {
    return get(END_LOCATION_KEY, RideEndLocation.class);
  }

  public Date getCompletedOn() {
    return get(COMPLETED_ON_KEY, Date.class);
  }

}
