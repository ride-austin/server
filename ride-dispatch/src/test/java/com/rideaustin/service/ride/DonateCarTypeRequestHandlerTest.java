package com.rideaustin.service.ride;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.google.maps.model.LatLng;
import com.rideaustin.model.City;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.user.DonateEmail;

public class DonateCarTypeRequestHandlerTest {

  @Mock
  private EmailService emailService;
  @Mock
  private CityCache cityCache;
  @Mock
  private Environment environment;

  private DonateCarTypeRequestHandler testedInstance;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DonateCarTypeRequestHandler(emailService, cityCache, environment);
  }

  @Test
  public void handleRequestSendsEmailAndThrowsError() throws RideAustinException, EmailException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Thank you for your donation. This is your confirmation that we received your pickup request (disregard the Oops title). Please leave your donation/package at your front door and a volunteer will pickup your items between 11am and 5pm. Thank you for your support. #HappyHolidays #RideForWhatMatters");

    final User rider = new User();
    rider.setFirstname("A");
    rider.setLastname("B");
    rider.setEmail("asdg@fdgs.er");
    final String address = "ABC";
    final LatLng startLocation = new LatLng(34.34681, -97.891641);
    final String comment = "JGJ";
    final long cityId = 1L;
    final City city = new City();
    when(cityCache.getCity(eq(cityId))).thenReturn(city);

    testedInstance.handleRequest(rider, address, startLocation, comment, cityId);

    verify(emailService).sendEmail(argThat(new BaseMatcher<DonateEmail>() {
      @Override
      public boolean matches(Object o) {
        final DonateEmail email = (DonateEmail) o;
        final Map<String, Object> emailModel = email.getModel();
        return emailModel.get("city").equals(city) &&
          emailModel.get("fullName").equals(rider.getFullName()) &&
          emailModel.get("email").equals(rider.getEmail()) &&
          emailModel.get("address").equals(address) &&
          emailModel.get("location").equals(String.format("%.7f,%.7f", startLocation.lat, startLocation.lng));
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }
}