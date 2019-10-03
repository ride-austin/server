package com.rideaustin.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import com.rideaustin.filter.ClientType;
import com.rideaustin.model.City;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.support.SupportService;

@RunWith(MockitoJUnitRunner.class)
public class SupportServiceTest {

  public static final String CONTACT_RIDEAUSTIN_COM = "contact@example.com";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private RideService rideService;
  @Mock
  private EmailService emailService;
  @Mock
  private Environment env;
  @Mock
  private DriverDslRepository driverDslRepository;
  @Mock
  private Ride ride;
  @Mock
  private CityService cityService;
  @Mock
  private SupportTopicService supportTopicService;
  @Mock
  private SupportService supportService;

  @Before
  public void setup() throws Exception {
    when(env.getProperty(eq("support.sender.email"), eq(CONTACT_RIDEAUSTIN_COM))).thenReturn(CONTACT_RIDEAUSTIN_COM);
    when(env.getProperty(eq("support.email"), eq(CONTACT_RIDEAUSTIN_COM))).thenReturn(CONTACT_RIDEAUSTIN_COM);
    when(cityService.getById(anyLong())).thenReturn(createCity());
    supportService = new SupportService(rideService, emailService, cityService, supportTopicService, env);
  }

  @Test
  public void testSendRideSupportEmail() throws Exception {
    when(rideService.getRide(anyLong())).thenReturn(ride);

    supportService.sendRideSupportEmail(createUser(), "Message", 1L, 1L, ClientType.RIDER);

    verify(emailService, times(1)).sendEmail(any());
  }

  @Test
  public void testSendDriverSupportEmail() throws Exception {
    supportService.sendGenericSupportEmail(createUser(), "Message", 1L, ClientType.RIDER);

    verify(emailService, times(1)).sendEmail(any());
  }

  private User createUser() {
    User user = new User();
    user.setEmail("test@ra.com");
    user.setFirstname("Test");
    user.setLastname("Sender");
    return user;
  }

  private City createCity() {
    City city = new City();
    city.setAppName("RideAustin");
    city.setContactEmail(CONTACT_RIDEAUSTIN_COM);
    city.setSupportEmail(CONTACT_RIDEAUSTIN_COM);
    return city;
  }
}
