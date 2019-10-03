package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringWriter;

import org.apache.commons.mail.EmailException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.rideaustin.model.City;
import com.rideaustin.model.DriverEmailHistoryItem;
import com.rideaustin.model.enums.CityEmailType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.DriverEmailReminder;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.DriverEmailReminderDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.thirdparty.PayoneerService;
import com.rideaustin.service.user.DriverReminderEmail;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class DriverEmailReminderServiceTest {

  @Spy
  @InjectMocks
  private DriverEmailReminderService testedInstance;
  @Mock
  private DriverEmailReminderDslRepository reminderDslRepository;
  @Mock
  private EmailService emailService;
  @Mock
  private Driver driver;
  @Mock
  private DriverEmailReminder reminder;
  @Mock
  private CityService cityService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private Configuration configuration;
  @Mock
  private DriverService driverService;
  @Mock
  private PayoneerService payoneerService;
  @Captor
  private ArgumentCaptor<Driver> capturedDriver;
  @Captor
  private ArgumentCaptor<DriverEmailReminder> capturedReminder;
  @Captor
  private ArgumentCaptor<DriverEmailHistoryItem> capturedHistory;
  private static final long DRIVER_ID = 1L;
  private static final long REMINDER_ID = 1L;
  private static final String TEMPLATE = "template.ftl";

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    User user = new User();
    user.setFirstname("A");
    user.setLastname("B");
    when(reminder.getEmailType()).thenReturn(CityEmailType.CONTACT);
    when(driverService.findDriver(eq(DRIVER_ID))).thenReturn(driver);
    when(reminderDslRepository.findOne(eq(REMINDER_ID))).thenReturn(reminder);
    when(cityService.getById(any())).thenReturn(generateCity());
    when(currentUserService.getUser()).thenReturn(user);
    when(reminder.getSubject()).thenReturn("Subject");
  }

  @Test
  public void sendReminder() throws Exception {
    testedInstance.sendReminder(DRIVER_ID, REMINDER_ID, null, null);

    verify(testedInstance).createReminderEmail(capturedReminder.capture(), capturedDriver.capture(), anyString(), anyString());
    verify(emailService, only()).sendEmail(any(DriverReminderEmail.class));
    verify(reminderDslRepository, times(1)).save(any(DriverEmailHistoryItem.class));
    assertEquals(reminder, capturedReminder.getValue());
    assertEquals(driver, capturedDriver.getValue());
  }

  @Test
  public void testSendReminderSetsContentForCustomEmails() throws Exception {
    String content = "content";
    when(reminder.isStoreContent()).thenReturn(true);

    testedInstance.sendReminder(DRIVER_ID, REMINDER_ID, content, null);

    verify(reminderDslRepository, times(1)).save(capturedHistory.capture());
    assertEquals(content, capturedHistory.getValue().getContent());
  }

  @Test(expected = ServerError.class)
  public void testSendReminderThrowsServerErrorOnEmailException() throws Exception {
    doThrow(new EmailException()).when(emailService).sendEmail(any(DriverReminderEmail.class));

    testedInstance.sendReminder(DRIVER_ID, REMINDER_ID, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMistakenlySendReminderWithoutContent() throws Exception {
    when(reminder.isStoreContent()).thenReturn(true);

    testedInstance.sendReminder(DRIVER_ID, REMINDER_ID, null, null);
  }

  @Test(expected = NotFoundException.class)
  public void testGetReminderHistoryContentThrowsNotFoundForNonexistentCommunication() throws Exception {
    long historyItemId = 1L;
    when(reminderDslRepository.findHistoryItem(historyItemId)).thenReturn(null);

    testedInstance.getReminderHistoryContent(historyItemId);
  }

  @Test
  public void testGetReminderHistoryContentProcessesEmailTemplate() throws Exception {
    long historyItemId = 1L;
    when(reminderDslRepository.findHistoryItem(historyItemId)).thenReturn(createHistoryItem());
    when(driverService.findDriver(anyLong())).thenReturn(driver);
    Template template = mock(Template.class);
    when(configuration.getTemplate(eq(TEMPLATE))).thenReturn(template);

    testedInstance.getReminderHistoryContent(historyItemId);

    verify(configuration).getTemplate(eq(TEMPLATE));
    verify(template).process(anyMap(), any(StringWriter.class));
  }

  private DriverEmailHistoryItem createHistoryItem() {
    DriverEmailHistoryItem item = new DriverEmailHistoryItem();
    DriverEmailReminder reminder = new DriverEmailReminder();
    reminder.setSubject("subject");
    reminder.setEmailTemplate(TEMPLATE);
    reminder.setEmailType(CityEmailType.CONTACT);
    item.setReminder(reminder);
    item.setDriverId(1L);
    return item;
  }

  private City generateCity() {
    City city = new City();
    city.setAppName("RideAustin");
    city.setOffice("Office Address");
    city.setContactEmail("contact@ridesomhere.com");
    city.setDocumentsEmail("docs@ridesomhere.com");
    city.setDriversEmail("drivers@ridesomhere.com");
    city.setOnboardingEmail("onb@ridesomhere.com");
    city.setSupportEmail("support@ridesomhere.com");
    city.onLoad();
    return city;
  }

}