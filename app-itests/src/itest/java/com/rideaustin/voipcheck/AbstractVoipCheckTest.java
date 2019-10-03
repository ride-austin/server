package com.rideaustin.voipcheck;

import java.util.Collections;

import javax.inject.Inject;

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
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.test.actions.AdministratorAction;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.UserAction;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.AdministratorFixture;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;
import com.rideaustin.test.utils.RandomUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractVoipCheckTest extends ITestProfileSupport {

  @Inject
  private PasswordEncoder passwordEncoder;

  @Inject
  protected RiderFixtureProvider riderFixtureProvider;

  @Inject
  protected DriverFixtureProvider driverFixtureProvider;

  @Inject
  protected RiderAction riderAction;

  @Inject
  protected DriverAction driverAction;

  @Inject
  protected AdministratorAction administratorAction;

  @Inject
  protected AdministratorFixture administratorFixture;
  protected Administrator administrator;

  @Inject
  protected UserAction userAction;

  protected final String validPhoneNumber = "+16505550001";

  protected final String voipPhoneNumber = "+16464812840";

  protected User newUnregisteredUser(String phoneNumber) {
    String randomEmail = RandomUtils.randomEmail();
    return User.builder()
      .phoneNumber(phoneNumber)
      .email(randomEmail)
      .facebookId(RandomUtils.randomName())
      .firstname(RandomUtils.randomName())
      .lastname(RandomUtils.randomName())
      .password(passwordEncoder.encode(randomEmail))
      .avatarTypesBitmask(AvatarType.RIDER.toBitMask())
      .gender(Gender.MALE)
      .userEnabled(true)
      .avatars(Collections.emptyList())
      .build();
  }

  public Rider updatedRider(Rider rider, final String phoneNumber) {
    return update(rider, Rider::new, target -> target.getUser().setPhoneNumber(phoneNumber));
  }
}

