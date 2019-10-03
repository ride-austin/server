package com.rideaustin.service;

import static com.rideaustin.test.util.TestUtils.RANDOM;
import static com.rideaustin.test.util.TestUtils.unwrapProxy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.validation.phone.PhoneNumberCheckerService;
import com.rideaustin.test.common.ITestProfileSupport;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class UserServiceIT extends ITestProfileSupport {

  @Inject
  private UserService userService;

  @Mock
  private PhoneNumberCheckerService phoneNumberCheckerService;

  @Mock
  private UserDslRepository userDslRepository;

  private User user = new User();

  private Driver driver = new Driver();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() throws Exception {
    super.setUp();
    user.setId(RANDOM.nextLong());
    user.setEmail("aaa@wp.pl");
    user.setPhoneNumber("123123");
    List<User> users = new ArrayList<>();
    User user2 = new User();
    user2.setId(RANDOM.nextLong());
    user2.setEmail("wasdsa@wp.pl");
    user2.setPhoneNumber("123123");
    users.add(user2);
    driver.setUser(user);

    MockitoAnnotations.initMocks(this);
    when(userDslRepository.isPhoneNumberInUse(any(String.class))).thenReturn(true);

    UserService unwrappedService = AopTestUtils.getUltimateTargetObject(userService);
    ReflectionTestUtils.setField(unwrappedService, "userDslRepository", userDslRepository);
    ReflectionTestUtils.setField(unwrappedService, "phoneNumberCheckerService", phoneNumberCheckerService);
  }

  @Test
  public void testNullNewObject() throws RideAustinException {
    userService.checkPhoneNumberAvailable("1234", null);
  }

  @Test
  public void testNullAsOldPhoneNumber() throws RideAustinException {
    userService.checkPhoneNumberAvailable(null, null);
  }

  @Test
  public void testEmptyStringAsOldEmail() throws RideAustinException {
    userService.checkPhoneNumberAvailable("", driver.getPhoneNumber());
  }

  @Test
  public void testEmptyStringAsNewEmail() throws RideAustinException {
    driver.getUser().setPhoneNumber("");
    userService.checkPhoneNumberAvailable("asdsd@wp.pl", driver.getPhoneNumber());
  }

  @Test
  public void testSameDataOldAndNew() throws RideAustinException {
    driver.getUser().setPhoneNumber("asdsd@wp.pl");
    userService.checkPhoneNumberAvailable("asdsd@wp.pl", driver.getPhoneNumber());
  }

  @Test(expected = BadRequestException.class)
  public void testProperParamData() throws RideAustinException {
    driver.getUser().setPhoneNumber("asds1@wp.pl");
    userService.checkPhoneNumberAvailable("asdsd@wp.pl", driver.getPhoneNumber());
  }

  @Test
  public void testProperParamDataButNotFind() throws Exception {
    UserDslRepository userDslRepository = mock(UserDslRepository.class);
    when(userDslRepository.findAnyByPhoneNumber(any(String.class))).thenReturn(null);
    Object unwrappedService = unwrapProxy(userService);
    ReflectionTestUtils.setField(unwrappedService, "userDslRepository", userDslRepository);

    driver.getUser().setPhoneNumber("asds1@wp.pl");
    userService.checkPhoneNumberAvailable("asdsd@wp.pl", driver.getPhoneNumber());
  }

  @Test
  public void getExistNoParams() throws Exception {

    thrown.expect(BadRequestException.class);
    thrown.expectMessage("Email or phone number is required");

    userService.checkIfUserNameAndPhoneNumberIsAvailable(null, null, null);
  }

  @Test
  public void getExistByEmailWhenExist() throws Exception {
    String emailToTest = "test@test.us";
    User user = new User();
    user.setId(RANDOM.nextLong());
    user.setEmail(emailToTest);
    when(userDslRepository.findByEmail(emailToTest)).thenReturn(user);

    thrown.expect(BadRequestException.class);
    thrown.expectMessage("This email address is already in use");

    userService.checkIfUserNameAndPhoneNumberIsAvailable(emailToTest, null, null);
  }

  @Test
  public void getExistByEmailWhenNotExist() throws Exception {
    String emailToTest = "test@test.us";
    when(userDslRepository.findByEmail(emailToTest)).thenReturn(null);

    userService.checkIfUserNameAndPhoneNumberIsAvailable(emailToTest, null, null);
  }

  @Test
  public void geExistByPhoneWhenExist() throws Exception {
    String phoneNumberToTest = "12345678";
    User user = new User();
    user.setId(RANDOM.nextLong());
    user.setPhoneNumber(phoneNumberToTest);
    List<User> users = new ArrayList<>();
    users.add(user);
    when(userDslRepository.findAnyByPhoneNumber(phoneNumberToTest)).thenReturn(users);

    thrown.expect(BadRequestException.class);
    thrown.expectMessage("This phone number is already in use");

    userService.checkIfUserNameAndPhoneNumberIsAvailable(null, phoneNumberToTest, null);
  }

  @Test
  public void geExistByPhoneWhenNotExist() throws Exception {
    String phoneNumberToTest = "12345678";
    when(userDslRepository.findAnyByPhoneNumber(phoneNumberToTest)).thenReturn(null);

    userService.checkIfUserNameAndPhoneNumberIsAvailable(null, phoneNumberToTest, null);
  }

  @Test
  public void getExistByPhoneAndEmailWhenExist() throws Exception {
    String phoneNumberToTest = "12345678";
    String emailToTest = "test@test.us";
    User user = new User();
    user.setId(RANDOM.nextLong());
    user.setEmail(emailToTest);
    user.setPhoneNumber(phoneNumberToTest);
    List<User> users = new ArrayList<>();
    users.add(user);
    when(userDslRepository.findAnyByPhoneNumber(phoneNumberToTest)).thenReturn(users);
    when(userDslRepository.findByEmail(emailToTest)).thenReturn(user);

    thrown.expect(BadRequestException.class);
    thrown.expectMessage("This user name and phone number are already in use.");

    userService.checkIfUserNameAndPhoneNumberIsAvailable(emailToTest, phoneNumberToTest, null);

  }

  @Test
  @WithMockUser(roles = AvatarType.NAME_ADMIN)
  public void getExistByPhoneAndEmailWhenMultipleExist() throws Exception {
    String phoneNumberToTest = "12345678";
    String emailToTest = "test@test.us";
    User user = new User();
    user.setId(RANDOM.nextLong());
    user.setEmail(emailToTest);
    user.setPhoneNumber(phoneNumberToTest);
    List<User> users = new ArrayList<>();
    User user2 = new User();
    user2.setId(RANDOM.nextLong());
    user2.setEmail(emailToTest);
    user2.setPhoneNumber(phoneNumberToTest);
    users.add(user2);
    when(userDslRepository.findAnyByPhoneNumber(phoneNumberToTest)).thenReturn(users);
    when(userDslRepository.findByEmail(emailToTest)).thenReturn(user);

    thrown.expect(BadRequestException.class);
    thrown.expectMessage("This user name and phone number are already in use.");

    userService.checkIfUserNameAndPhoneNumberIsAvailable(emailToTest, phoneNumberToTest, null);
  }
}
