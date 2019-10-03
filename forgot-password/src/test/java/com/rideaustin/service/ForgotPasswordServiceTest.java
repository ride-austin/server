package com.rideaustin.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.apache.commons.mail.EmailException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rideaustin.model.City;
import com.rideaustin.model.PasswordVerificationToken;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.PasswordVerificationTokenDslRepository;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.user.PasswordResetEmail;
import com.rideaustin.service.user.PasswordResetSuccessEmail;
import com.rideaustin.utils.CryptUtils;

public class ForgotPasswordServiceTest {

  @Mock
  private Environment environment;
  @Mock
  private CityService cityService;
  @Mock
  private EmailService emailService;
  @Mock
  private PasswordEncoder encoder;
  @Mock
  private PasswordVerificationTokenDslRepository repository;
  @Mock
  private UserDslRepository userDslRepository;
  @Mock
  private CryptUtils cryptUtils;

  private ForgotPasswordService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ForgotPasswordService(environment, cityService, emailService, encoder, repository, userDslRepository,
      cryptUtils);
  }

  @Test(expected = ServerError.class)
  public void sendPasswordReminderEmailThrowsErrorOnFailingSending() throws EmailException, ServerError {
    final PasswordVerificationToken token = new PasswordVerificationToken();
    token.setToken("ABC");
    final City city = new City();
    city.setContactEmail("a@b.c");
    city.setAppName("ASD");
    doThrow(new EmailException()).when(emailService).sendEmail(any(PasswordResetEmail.class));
    when(repository.save(any(PasswordVerificationToken.class))).thenReturn(token);
    when(cityService.getCityForCurrentClientAppVersionContext()).thenReturn(city);

    testedInstance.sendPasswordReminderEmail(new User());
  }

  @Test
  public void sendPasswordReminderEmailCallsEmailService() throws ServerError, EmailException {
    final PasswordVerificationToken token = new PasswordVerificationToken();
    token.setToken("ABC");
    final City city = new City();
    city.setContactEmail("a@b.c");
    city.setAppName("ASD");
    when(repository.save(any(PasswordVerificationToken.class))).thenReturn(token);
    when(cityService.getCityForCurrentClientAppVersionContext()).thenReturn(city);

    testedInstance.sendPasswordReminderEmail(new User());

    verify(emailService, times(1)).sendEmail(any(PasswordResetEmail.class));
  }

  @Test
  public void resetPasswordReturnsFalseWhenTokenNotFound() {
    when(repository.findToken(anyString())).thenReturn(null);

    final boolean result = testedInstance.resetPassword("ABC");

    assertFalse(result);
  }

  @Test
  public void resetPasswordReturnsFalseWhenEmailIsNotSent() throws EmailException {
    final PasswordVerificationToken token = new PasswordVerificationToken();
    final User user = new User();
    final City city = new City();
    city.setContactEmail("a@b.c");
    city.setAppName("ASD");
    when(repository.findToken(anyString())).thenReturn(token);
    when(userDslRepository.findAnyByEmail(anyString())).thenReturn(user);
    doThrow(new EmailException()).when(emailService).sendEmail(any(PasswordResetSuccessEmail.class));
    when(cityService.getDefaultCity()).thenReturn(city);

    final boolean result = testedInstance.resetPassword("ABC");

    assertFalse(result);
  }

  @Test
  public void resetPasswordChangesPassword() {
    final PasswordVerificationToken token = new PasswordVerificationToken();
    final User user = new User();
    final City city = new City();
    city.setContactEmail("a@b.c");
    city.setAppName("ASD");
    when(repository.findToken(anyString())).thenReturn(token);
    when(userDslRepository.findAnyByEmail(anyString())).thenReturn(user);
    when(cityService.getDefaultCity()).thenReturn(city);

    final boolean result = testedInstance.resetPassword("ABC");

    assertTrue(result);
    verify(userDslRepository, times(1)).changePassword(eq(user.getId()), anyString());
  }

  @Test
  public void resetPasswordExpiresToken() throws InterruptedException {
    final PasswordVerificationToken token = new PasswordVerificationToken();
    final User user = new User();
    final City city = new City();
    city.setContactEmail("a@b.c");
    city.setAppName("ASD");
    when(repository.findToken(anyString())).thenReturn(token);
    when(userDslRepository.findAnyByEmail(anyString())).thenReturn(user);
    when(cityService.getDefaultCity()).thenReturn(city);

    final boolean result = testedInstance.resetPassword("ABC");

    Thread.sleep(100);

    assertTrue(result);
    verify(repository, times(1)).save(argThat(new BaseMatcher<PasswordVerificationToken>() {
      @Override
      public boolean matches(Object o) {
        final PasswordVerificationToken token = (PasswordVerificationToken) o;
        return token.getExpiresOn().before(new Date());
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }
}