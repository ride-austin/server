package com.rideaustin.jobs;

import java.time.LocalDate;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rideaustin.service.CityService;
import com.rideaustin.service.DriverService;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.user.LicenseExpiredEmail;
import com.rideaustin.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExpiredDriverLicenseJob extends BaseJob {

  @Inject
  private EmailService emailService;

  @Inject
  private DriverService driverService;

  @Inject
  private CityService cityService;

  private Date expirationLimit;

  private Date notificationLimit;

  @Value("${jobs.driver_license_expire.expire_weeks}")
  public void setExpireWeeks(Long expireWeeks) {
    expirationLimit = DateUtils.localDateToDate(LocalDate.now().plusWeeks(expireWeeks));
  }

  @Value("${jobs.driver_license_expire.notification_period_weeks}")
  public void setNotificationPeriodWeeks(Long notificationPeriodWeeks) {
    notificationLimit = DateUtils.localDateToDate(LocalDate.now().minusWeeks(notificationPeriodWeeks));
  }

  @Override
  protected String getDescription() {
    return "sending expired driver license notification";
  }

  @Override
  public void executeInternal() {

    driverService.getExpiredLicenseDrivers(expirationLimit, notificationLimit)
      .forEach(driver -> {
        try {
          log.info("Sending driver license expiration notification to {}", driver.getEmail());
          emailService.sendEmail(new LicenseExpiredEmail(driver, cityService.getById(driver.getCityId())));
          driverService.saveExpiredLicenseNotification(driver);
        } catch (EmailException e) {
          log.error("Failed to send expired license notification to {}", driver.getEmail(), e);
        }
      });
  }

}
