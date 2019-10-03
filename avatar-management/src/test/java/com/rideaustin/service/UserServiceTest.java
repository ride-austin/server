package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.BaseEntityPhoto;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.SignUpException;
import com.rideaustin.rest.model.UpdateUserDto;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.BlockedDeviceService;
import com.rideaustin.service.validation.phone.PhoneNumberCheckerService;
import com.rideaustin.user.tracking.model.UserTrack;
import com.rideaustin.user.tracking.repo.dsl.UserTrackDslRepository;

public class UserServiceTest {

  @Mock
  private UserDslRepository userDslRepository;
  @Mock
  private UserTrackDslRepository userTrackDslRepository;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private RideDslRepository rideRepository;
  @Mock
  private SessionService sessionService;
  @Mock
  private ActiveDriversService activeDriversService;
  @Mock
  private PhoneNumberCheckerService phoneNumberCheckerService;
  @Mock
  private BlockedDeviceService blockedDeviceService;
  @Mock
  private PasswordEncoder encoder;
  @Mock
  private S3StorageService s3StorageService;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private UserService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new UserService(userDslRepository, userTrackDslRepository, currentUserService, rideRepository, sessionService,
      activeDriversService, phoneNumberCheckerService, blockedDeviceService, encoder, s3StorageService);
  }

  @Test
  public void checkPhoneNumberAvailableThrowsError() throws BadRequestException {
    final String newPhoneNumber = "+15125555556";
    when(userDslRepository.isPhoneNumberInUse(newPhoneNumber)).thenReturn(true);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Entered phone number is already in use.");

    testedInstance.checkPhoneNumberAvailable("+15125555555", newPhoneNumber);
  }

  @Test
  public void logOutUserDeactivatesDriver() throws BadRequestException {
    when(userDslRepository.getWithDependencies(anyLong())).thenReturn(new User());

    testedInstance.logOutUser(1L, ApiClientAppType.MOBILE_DRIVER);

    verify(activeDriversService).deactivateAsDriver();
  }

  @Test
  public void logOutUserThrowsErrorWhenRequestedRideExists() throws BadRequestException {
    final User user = new User();
    user.addAvatar(new Rider());
    when(userDslRepository.getWithDependencies(anyLong())).thenReturn(user);
    when(rideRepository.findByRiderAndStatus(any(Rider.class), eq(RideStatus.REQUESTED)))
      .thenReturn(Collections.singletonList(new Ride()));

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("You cannot logout while you are requesting a ride");

    testedInstance.logOutUser(1L, ApiClientAppType.MOBILE_RIDER);
  }

  @Test
  public void updateUserThrowsForbiddenExceptionWhenUserChangesOther() throws RideAustinException {
    final long userId = 1L;
    final User user = new User();
    user.setId(userId);
    when(userDslRepository.findOne(anyLong())).thenReturn(user);
    final User currentUser = new User();
    currentUser.setId(2L);
    when(currentUserService.getUser()).thenReturn(currentUser);

    expectedException.expect(ForbiddenException.class);

    testedInstance.updateUser(userId, new UpdateUserDto());
  }

  @Test
  public void updateUserThrowsErrorWhenEmailAndPhoneAreInvalid() throws RideAustinException {
    final long userId = 1L;
    final User user = new User();
    user.setId(userId);
    user.setPhoneNumber("+15125555555");
    when(userDslRepository.findOne(anyLong())).thenReturn(user);
    final User currentUser = new User();
    currentUser.setId(1L);
    when(currentUserService.getUser()).thenReturn(currentUser);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Email or phone number is required");

    testedInstance.updateUser(userId, new UpdateUserDto());
  }

  @Test
  public void updateUserThrowsErrorWhenPhoneIsAlreadyUsed() throws RideAustinException {
    final long userId = 1L;
    final User user = new User();
    user.setId(userId);
    user.setPhoneNumber("+15125555555");
    when(userDslRepository.findOne(anyLong())).thenReturn(user);
    final User currentUser = new User();
    currentUser.setId(1L);
    when(currentUserService.getUser()).thenReturn(currentUser);

    final UpdateUserDto updated = new UpdateUserDto();
    updated.setPhoneNumber("+15125555556");

    when(userDslRepository.findAnyByPhoneNumber(updated.getPhoneNumber())).thenReturn(Collections.singletonList(new User()));

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("This phone number is already in use");

    testedInstance.updateUser(userId, updated);
  }

  @Test
  public void updateUserSavesData() throws RideAustinException {
    final long userId = 1L;
    final User user = new User();
    user.setId(userId);
    user.setPhoneNumber("+15125555555");
    when(userDslRepository.findOne(anyLong())).thenReturn(user);
    final User currentUser = new User();
    currentUser.setId(1L);
    when(currentUserService.getUser()).thenReturn(currentUser);

    final UpdateUserDto updated = new UpdateUserDto();
    updated.setPhoneNumber("+15125555556");

    when(userDslRepository.findAnyByPhoneNumber(updated.getPhoneNumber())).thenReturn(Collections.emptyList());

    testedInstance.updateUser(userId, updated);

    verify(userDslRepository).save(user);
  }

  @Test
  public void createUserThrowsErrorWhenDeviceIsBlocked() throws RideAustinException {
    final String deviceId = "device-id";
    final HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("User-Device-Id")).thenReturn(deviceId);
    when(blockedDeviceService.isInBlocklist(deviceId)).thenReturn(true);

    expectedException.expect(SignUpException.class);
    expectedException.expectMessage(Constants.ErrorMessages.DEVICE_IS_BLOCKED);

    testedInstance.createUser("abc@def.ee", null, "A", "B", "password",
      "+15125555555", "url", true, request);
  }

  @Test
  public void createUserThrowsErrorWhenUserAlreadyExists() throws RideAustinException {
    final HttpServletRequest request = mock(HttpServletRequest.class);

    final String email = "abc@def.ee";
    when(userDslRepository.findAnyByEmail(email)).thenReturn(new User());

    expectedException.expect(SignUpException.class);
    expectedException.expectMessage(Constants.ErrorMessages.USER_ALREADY_EXISTS);

    testedInstance.createUser(email, null, "A", "B", "password",
      "+15125555555", "url", true, request);
  }

  @Test
  public void createUserUnverifiesUserWhenPhoneNumberIsNotVerified() throws RideAustinException {
    final HttpServletRequest request = mock(HttpServletRequest.class);

    when(userDslRepository.save(any(User.class))).thenAnswer((Answer<User>) invocation -> (User) invocation.getArguments()[0]);

    final User result = testedInstance.createUser("abc@def.ee", null, "A", "B", "password",
      "+15125555555", "url", false, request);

    assertFalse(result.getUserEnabled());
    assertFalse(result.isPhoneNumberVerified());
  }

  @Test
  public void createUserVerifiesUserWhenPhoneNumberIsVerified() throws RideAustinException {
    final HttpServletRequest request = mock(HttpServletRequest.class);

    when(userDslRepository.save(any(User.class))).thenAnswer((Answer<User>) invocation -> (User) invocation.getArguments()[0]);

    final User result = testedInstance.createUser("abc@def.ee", null, "A", "B", "password",
      "+15125555555", "url", true, request);

    assertTrue(result.getUserEnabled());
    assertTrue(result.isPhoneNumberVerified());
  }

  @Test
  public void createUserSetsSocialId() throws RideAustinException {
    final HttpServletRequest request = mock(HttpServletRequest.class);

    when(userDslRepository.save(any(User.class))).thenAnswer((Answer<User>) invocation -> (User) invocation.getArguments()[0]);

    final String socialId = "123";
    final User result = testedInstance.createUser("abc@def.ee", socialId, "A", "B", "password",
      "+15125555555", "url", true, request);

    assertEquals(socialId, result.getFacebookId());
    assertEquals(String.format("https://graph.facebook.com/%s/picture?type=large", socialId), result.getPhotoUrl());
  }

  @Test
  public void createUserUploadsPhoto() throws RideAustinException {
    final HttpServletRequest request = mock(HttpServletRequest.class);
    final String userPhoto = "data";
    final String url = "url";

    when(userDslRepository.save(any(User.class))).thenAnswer((Answer<User>) invocation -> (User) invocation.getArguments()[0]);
    when(s3StorageService.uploadPublicFile(BaseEntityPhoto.USER_PHOTOS, userPhoto)).thenReturn(url);

    final User result = testedInstance.createUser("abc@def.ee", null, "A", "B", "password",
      "+15125555555", userPhoto, true, request);

    assertEquals(url, result.getPhotoUrl());
  }

  @Test
  public void createUserTracksUserSource() throws RideAustinException {
    final HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameterMap()).thenReturn(ImmutableMap.of(
      "utm_source", new String[]{"source"}
    ));
    when(userDslRepository.save(any(User.class))).thenAnswer((Answer<User>) invocation -> (User) invocation.getArguments()[0]);

    testedInstance.createUser("abc@def.ee", null, "A", "B", "password",
      "+15125555555", "azaza", true, request);

    verify(userTrackDslRepository).save(any(UserTrack.class));
  }
}