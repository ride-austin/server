package com.rideaustin.jobs;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

import com.rideaustin.service.CityService;
import com.rideaustin.service.email.EmailService;

import lombok.Setter;

public abstract class BaseEmailJob extends BaseJob {

  @Setter(onMethod = @__(@Inject))
  protected EmailService emailService;

  @Setter(onMethod = @__(@Inject))
  protected CityService cityService;

  @Setter
  protected LocalDate reportDate;
  @Setter
  protected Long driverId;
  @Setter
  protected List<String> recipients;
}
