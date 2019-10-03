package com.rideaustin.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.DriverEmailHistoryItem;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.DriverEmailReminder;
import com.rideaustin.repo.dsl.DriverEmailReminderDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.thirdparty.PayoneerService;
import com.rideaustin.service.user.DriverReminderEmail;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverEmailReminderService {

  private final DriverEmailReminderDslRepository reminderDslRepository;
  private final EmailService emailService;
  private final CityService cityService;
  private final DriverService driverService;
  private final Configuration configuration;
  private final CurrentUserService currentUserService;
  private final PayoneerService payoneerService;

  @Transactional
  public void sendReminder(long driverId, long reminderId, String content, String subject) throws RideAustinException {
    DriverEmailReminder reminder = reminderDslRepository.findOne(reminderId);

    if (reminder.isStoreContent() && StringUtils.isBlank(content)) {
      throw new IllegalArgumentException("Parameter 'content' is empty");
    }

    Driver driver = driverService.findDriver(driverId);
    try {
      emailService.sendEmail(createReminderEmail(reminder, driver, content, subject));
      trackReminderHistory(driverId, content, reminder);
    } catch (EmailException e) {
      throw new ServerError(e);
    }
  }

  public List<DriverEmailHistoryItem> getHistory(long driverId) {
    return reminderDslRepository.findHistory(driverId);
  }

  public String getReminderContent(Long reminderId, Long driverId) throws RideAustinException {

    try {
      Driver driver = driverService.findDriver(driverId);
      DriverEmailReminder driverEmailReminder = reminderDslRepository.findOne(reminderId);
      String content = "";
      if (driverEmailReminder.isStoreContent()) {
        content = "Custom text";
      }
      DriverReminderEmail email = new DriverReminderEmail(driverEmailReminder, content, "", driver,
        cityService.getById(driver.getCityId()));

      StringWriter sw = new StringWriter();
      Template template = configuration.getTemplate(driverEmailReminder.getEmailTemplate());
      template.process(email.getModel(), sw);
      return sw.toString();
    } catch (EmailException | IOException | TemplateException e) {
      throw new ServerError(e);
    }
  }

  public String getReminderHistoryContent(Long reminderHistoryId) throws RideAustinException {
    DriverEmailHistoryItem historyItem = Optional.ofNullable(reminderDslRepository.findHistoryItem(reminderHistoryId))
      .orElseThrow(() -> new NotFoundException("Communication not found"));
    String content = "";
    if (historyItem.getContent() != null) {
      content = historyItem.getContent();
    }
    Driver driver = driverService.findDriver(historyItem.getDriverId());
    try {
      DriverReminderEmail email = new DriverReminderEmail(historyItem.getReminder(), content, "",
        driver, cityService.getById(driver.getCityId()));
      StringWriter sw = new StringWriter();
      Template template = configuration.getTemplate(historyItem.getReminder().getEmailTemplate());
      template.process(email.getModel(), sw);
      return sw.toString();
    } catch (EmailException | IOException | TemplateException e) {
      throw new ServerError(e);
    }
  }

  public void sendActivationEmail(long driverId, Long cityId) throws RideAustinException {
    DriverEmailReminder activation = reminderDslRepository.findActivationEmail(cityId);
    sendReminder(driverId, activation.getId(), null, null);
  }

  protected DriverReminderEmail createReminderEmail(DriverEmailReminder reminder, Driver driver, String content, String subject) throws EmailException {
    try {
      driver.setPayoneerSignupUrl(payoneerService.getSignupURL(driver.getPayoneerId()));
    } catch (RideAustinException e) {
      log.error("Failed to create a payoneer signup link", e);
    }
    return new DriverReminderEmail(reminder, content, subject, driver, cityService.getById(driver.getCityId()));
  }

  private void trackReminderHistory(long driverId, String content, DriverEmailReminder reminder) {
    DriverEmailHistoryItem historyItem = new DriverEmailHistoryItem();
    historyItem.setActor(currentUserService.getUser().getFullName());
    historyItem.setDriverId(driverId);
    historyItem.setReminder(reminder);
    if (reminder.isStoreContent()) {
      historyItem.setContent(content);
    }
    reminderDslRepository.save(historyItem);
  }
}
