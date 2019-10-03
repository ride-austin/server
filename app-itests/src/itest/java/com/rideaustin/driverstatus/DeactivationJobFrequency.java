package com.rideaustin.driverstatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.TestPropertySource;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@TestPropertySource(properties = {
  "jobs.active_driver_deactivate.threshold_seconds=4",
  "jobs.active_driver_away.threshold_seconds=2",
  "jobs.active_driver_deactivate.cron=0 0 0/12 ? * *"
})
public @interface DeactivationJobFrequency {
}
