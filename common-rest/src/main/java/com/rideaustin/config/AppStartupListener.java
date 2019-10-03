package com.rideaustin.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.quartz.SchedulerException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.rideaustin.driverstatistic.jobs.DriverStatisticJob;
import com.rideaustin.jobs.ActiveDriverDeactivateJob;
import com.rideaustin.jobs.DispatchAreaUpdateJob;
import com.rideaustin.jobs.EarningsEmailJob;
import com.rideaustin.jobs.RideUpgradeRequestExpirationJob;
import com.rideaustin.service.SchedulerService;
import com.rideaustin.service.event.ExpiredEventsCleanUpJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!itest")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AppStartupListener {

  protected final Environment env;
  protected final SchedulerService schedulerService;

  @PostConstruct
  protected void onStartup() throws SchedulerException {
    try {
      log.info("Startup code start...");
      schedulerService.scheduleJob(ActiveDriverDeactivateJob.class, env.getProperty("jobs.active_driver_deactivate.cron"));
      schedulerService.scheduleJob(EarningsEmailJob.class, env.getProperty("jobs.driver_earnings.cron"));
      schedulerService.scheduleJob(DispatchAreaUpdateJob.class, env.getProperty("jobs.dispatch.area.update.cron"));
      schedulerService.scheduleJob(ExpiredEventsCleanUpJob.class, env.getProperty("jobs.expired_events_cleanup_job.cron"));
      schedulerService.scheduleJob(RideUpgradeRequestExpirationJob.class, env.getProperty("jobs.expired_upgrade_requests.cron"));
      schedulerService.scheduleJob(DriverStatisticJob.class, env.getProperty("jobs.driver_statistics.cron"));
    } finally {
      log.info("Startup code end...");
    }
  }

}
