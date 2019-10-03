package com.rideaustin.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.quartz.SchedulerException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.jobs.CampaignTripReportJob;
import com.rideaustin.jobs.CustomPaymentsEmailJob;
import com.rideaustin.jobs.DocumentExpirationJob;
import com.rideaustin.jobs.ExpiredDriverLicenseJob;
import com.rideaustin.jobs.PayoneerPaymentJob;
import com.rideaustin.jobs.PayoneerStatusUpdateJob;
import com.rideaustin.jobs.PromocodeRedemptionActivityJob;
import com.rideaustin.jobs.RidePendingPaymentJob;
import com.rideaustin.jobs.SurgePricingAreasJob;
import com.rideaustin.jobs.export.DriversExportJob;
import com.rideaustin.rest.model.PeriodicReportType;
import com.rideaustin.service.SchedulerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!itest")
public class AdminAppStartupListener extends AppStartupListener {

  @Inject
  public AdminAppStartupListener(Environment env, SchedulerService schedulerService) {
    super(env, schedulerService);
  }

  @Override
  @PostConstruct
  protected void onStartup() throws SchedulerException {
    try {
      log.info("Startup admin code start...");
      schedulerService.scheduleJob(ExpiredDriverLicenseJob.class, env.getProperty("jobs.driver_license_expire.cron"));
      schedulerService.scheduleJob(PayoneerPaymentJob.class, env.getProperty("jobs.payoneer_report.cron"));
      schedulerService.scheduleJob(DriversExportJob.class, env.getProperty("jobs.drivers_report.cron"));
      schedulerService.scheduleJob(SurgePricingAreasJob.class, env.getProperty("jobs.surge_pricing.cron"));
      schedulerService.scheduleJob(CustomPaymentsEmailJob.class, env.getProperty("jobs.driver_custom_earnings.cron"));
      schedulerService.scheduleJob(PayoneerStatusUpdateJob.class, env.getProperty("jobs.payoneer.status.update"));
      schedulerService.scheduleJob(RidePendingPaymentJob.class, env.getProperty("jobs.pending_ride_payments_job.cron"));
      schedulerService.scheduleJob(DocumentExpirationJob.class, env.getProperty("jobs.expired_documents.cron"));
      schedulerService.scheduleJob(PromocodeRedemptionActivityJob.class, env.getProperty("jobs.promocode_redemption_activity_job.cron"));
      schedulerService.scheduleJob(CampaignTripReportJob.class, env.getProperty("jobs.campaign_trips.cron"));
      schedulerService.scheduleJob(CampaignTripReportJob.class, env.getProperty("jobs.campaign_trips.cron.daily"),
        ImmutableMap.of("type", PeriodicReportType.DAILY));
    } finally {
      log.info("Startup admin code end...");
    }
  }
}
