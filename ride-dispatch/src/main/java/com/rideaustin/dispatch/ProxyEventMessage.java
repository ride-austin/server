package com.rideaustin.dispatch;

import java.io.Serializable;

import org.springframework.messaging.MessageHeaders;

import com.rideaustin.service.model.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProxyEventMessage implements Serializable {

  private static final long serialVersionUID = 1635138461681684L;

  private long rideId;
  private Events event;
  private MessageHeaders headers;
}
