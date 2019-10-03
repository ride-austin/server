package com.rideaustin.service.email;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.Gender;
import com.rideaustin.model.user.User;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.test.actions.AdministratorAction;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.UserAction;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.AdministratorFixture;
import com.rideaustin.test.fixtures.UserFixture;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;
import com.rideaustin.test.fixtures.providers.UserFixtureProvider;
import com.rideaustin.test.utils.RandomUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractEmailTest extends ITestProfileSupport {

  @Inject
  protected EmailCheckerService emailCheckerService;

  @Inject
  protected RiderFixtureProvider riderFixtureProvider;

  @Inject
  protected RiderAction riderAction;

  @Inject
  protected DriverFixtureProvider driverFixtureProvider;

  @Inject
  protected DriverAction driverAction;

  @Inject
  protected AdministratorFixture administratorFixture;
  protected Administrator administrator;

  @Inject
  protected AdministratorAction administratorAction;

  @Inject
  protected UserFixtureProvider userFixtureProvider;

  @Inject
  protected UserAction userAction;

  @Inject
  private PasswordEncoder passwordEncoder;

  protected Date startDate;

  private static final long SLEEP_TIME = 2000L;

  private static final int FETCH_COUNT = 5;

  @Before
  public void baseSetUp() throws Exception {
    super.setUp();
    startDate = nowMinusOneSecond();
  }

  @After
  public void baseTearDown() {
    emailCheckerService.close();
  }

  protected List<InterceptingEmailService.Email> fetchEmailsWithSleep() {
    sleep(SLEEP_TIME);
    return emailCheckerService.fetchEmails(FETCH_COUNT);
  }

  protected User newUnregisteredUser() {
    String randomEmail = RandomUtils.randomEmail();
    return User.builder()
      .phoneNumber(RandomUtils.randomPhoneNumber())
      .email(randomEmail)
      .firstname(RandomUtils.randomName())
      .lastname(RandomUtils.randomName())
      .password(passwordEncoder.encode(randomEmail))
      .avatarTypesBitmask(AvatarType.RIDER.toBitMask())
      .gender(Gender.MALE)
      .userEnabled(true)
      .avatars(Collections.emptyList())
      .build();
  }

  protected User newRegisteredUser() {
    UserFixture userFixture = userFixtureProvider.create();
    return userFixture.getFixture();
  }

  protected Date nowMinusOneSecond() {
    return Date.from(LocalDateTime.now().minusSeconds(1)
      .atZone(ZoneOffset.systemDefault()).toInstant());
  }

}
