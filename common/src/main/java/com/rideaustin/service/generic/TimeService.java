package com.rideaustin.service.generic;

import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Service;

@Service
public class TimeService {

  public Date getCurrentDate() {
    return new Date();
  }

  public Instant getInstant() {
    return Instant.now();
  }
}
