package com.rideaustin.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.mail.EmailException;
import org.apache.commons.validator.routines.EmailValidator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.rideaustin.model.City;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.thirdparty.AmazonSNSService;
import com.rideaustin.service.thirdparty.CommunicationServiceFactory;
import com.rideaustin.service.user.ReferADriverEmail;
import com.rideaustin.service.user.ReferADriverSMS;

public class ReferralServiceTest {

  private static long ID = 1L;
  private static String PHONE_NUMBER = "555123123123";
  private static String EMAIL_GMAIL_COM = "email@gmail.com";
  private static String CONTACT_RIDEAUSTIN_COM = "contact@example.com";
  private static String INVALID_EMAIL = "invalid_email";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private CommunicationServiceFactory communicationServiceFactory;
  @Mock
  private DriverService driverService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private CityService cityService;
  @Mock
  private EmailService emailService;

  @Mock
  private EmailValidator emailValidator;

  @Mock
  private Environment environment;
  @Mock
  private AmazonSNSService amazonSNSService;


  private ReferralService referralService;

  private Driver driver;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    referralService = new ReferralService(communicationServiceFactory, driverService, currentUserService, cityService,
      emailService, environment);

    driver = new Driver();
    driver.setUser(new User());

    when(communicationServiceFactory.createCommunicationService()).thenReturn(amazonSNSService);
    when(cityService.getCityForCurrentClientAppVersionContext()).thenReturn(createCity());
  }

  @Test
  public void testReferAFriendWithAEmail() throws RideAustinException, EmailException {
    when(driverService.findDriver(anyLong(), any(User.class))).thenReturn(driver);
    when(environment.getProperty("sender.email", CONTACT_RIDEAUSTIN_COM)).thenReturn(CONTACT_RIDEAUSTIN_COM);
    when(emailValidator.isValid(EMAIL_GMAIL_COM)).thenReturn(true);

    referralService.referAFriendByEmail(ID, EMAIL_GMAIL_COM, null);
    verify(emailService, times(1)).sendEmail(any(ReferADriverEmail.class));
  }

  @Test
  public void testReferAFriendWithEmailNoEmail() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Please enter a valid email address");

    referralService.referAFriendByEmail(ID, null, 1L);
  }

  @Test
  public void testReferAFriendWithEmailInvalidEmail() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Please enter a valid email address");

    referralService.referAFriendByEmail(ID, INVALID_EMAIL, 1L);
  }

  @Test
  public void testReferAFriendWithSMSNoNumber() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Please enter a valid phone number");

    referralService.referAFriendBySMS(ID, null, 1L);
  }

  @Test
  public void testReferAFriendWithSMS() throws RideAustinException, EmailException {
    when(driverService.findDriver(anyLong(), any(User.class))).thenReturn(driver);

    referralService.referAFriendBySMS(ID, PHONE_NUMBER, null);
    verify(amazonSNSService, times(1)).sendSms(any(ReferADriverSMS.class));
  }

  private City createCity() {
    City city = new City();
    city.setContactEmail(CONTACT_RIDEAUSTIN_COM);
    city.setSupportEmail(CONTACT_RIDEAUSTIN_COM);
    return city;
  }


}