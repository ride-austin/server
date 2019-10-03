package com.rideaustin.service;

import static com.rideaustin.Constants.ErrorMessages.DEVICE_IS_BLOCKED;
import static com.rideaustin.Constants.ErrorMessages.USER_ALREADY_EXISTS;
import static com.rideaustin.utils.FraudLogUtil.extractIPAddress;
import static com.rideaustin.utils.FraudLogUtil.fraudLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import com.rideaustin.model.BaseEntityPhoto;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.enums.SessionClosingReason;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.SignUpException;
import com.rideaustin.rest.model.UpdateUserDto;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.BlockedDeviceService;
import com.rideaustin.service.validation.phone.PhoneNumberCheckerService;
import com.rideaustin.user.tracking.model.UserTrack;
import com.rideaustin.user.tracking.model.UserTrackData;
import com.rideaustin.user.tracking.repo.dsl.UserTrackDslRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserService {

  private final UserDslRepository userDslRepository;
  private final UserTrackDslRepository userTrackDslRepository;
  private final CurrentUserService currentUserService;
  private final RideDslRepository rideRepository;
  private final SessionService sessionService;
  private final ActiveDriversService activeDriversService;
  private final PhoneNumberCheckerService phoneNumberCheckerService;
  private final BlockedDeviceService blockedDeviceService;
  private final PasswordEncoder encoder;
  private final S3StorageService s3StorageService;

  public void checkPhoneNumberAvailable(final String currentPhoneNumber, final String newPhoneNumber) throws BadRequestException {
    if (
      StringUtils.isNotEmpty(newPhoneNumber)
      && StringUtils.isNotEmpty(currentPhoneNumber)
      && !currentPhoneNumber.equals(newPhoneNumber)
      && userDslRepository.isPhoneNumberInUse(newPhoneNumber)
    ) {
      throw new BadRequestException("Entered phone number is already in use.");
    }
  }

  public void checkPhoneNumberConstraints(String phoneNumber) throws RideAustinException {
    phoneNumberCheckerService.check(phoneNumber);
  }

  public void logOutUser(Long userId, ApiClientAppType apiClientAppType) throws BadRequestException {
    User user = userDslRepository.getWithDependencies(userId);
    checkUserRides(user);
    sessionService.endSession(user, apiClientAppType, SessionClosingReason.LOGOUT);
    if (ApiClientAppType.MOBILE_DRIVER.equals(apiClientAppType)) {
      activeDriversService.deactivateAsDriver();
    }
  }

  public User updateUser(long id, UpdateUserDto user) throws RideAustinException {
    User u = getUser(id);
    if (!u.equals(currentUserService.getUser())) {
      throw new ForbiddenException();
    }
    u.setFirstname(user.getFirstname());
    u.setLastname(user.getLastname());
    u.setNickName(user.getNickName());
    u.setGender(user.getGender());
    if (!u.getPhoneNumber().equals(user.getPhoneNumber())) {
      checkIfUserNameAndPhoneNumberIsAvailable(null, user.getPhoneNumber(), u);
    }
    u.setPhoneNumber(user.getPhoneNumber());
    u = userDslRepository.save(u);
    currentUserService.setUser(u);
    return u;
  }

  @Nonnull
  public User getUser(@PathVariable long id) throws NotFoundException {
    return Optional.ofNullable(userDslRepository.findOne(id))
      .orElseThrow(() -> new NotFoundException("User not found"));
  }

  public void checkIfUserNameAndPhoneNumberIsAvailable(String email, String phoneNumber, User ignoreUser) throws RideAustinException {
    boolean checkEmail = !StringUtils.isEmpty(email);
    boolean checkPhoneNumber = !StringUtils.isEmpty(phoneNumber);
    if (!checkEmail && !checkPhoneNumber) {
      throw new BadRequestException("Email or phone number is required");
    }

    if (checkPhoneNumber && (ignoreUser == null || !ignoreUser.isDriver())) {
      checkPhoneNumberConstraints(phoneNumber);
    }

    User userByEmail = null;
    List<User> usersByPhoneNumber = new ArrayList<>();
    if (checkEmail) {
      userByEmail = userDslRepository.findByEmail(email);
    }
    if (checkPhoneNumber) {
      usersByPhoneNumber = userDslRepository.findAnyByPhoneNumber(phoneNumber);
    }

    if (ignoreUser != null) {
      if (userByEmail != null && userByEmail.getId() == ignoreUser.getId()) {
        userByEmail = null;
      }
      if (CollectionUtils.isNotEmpty(usersByPhoneNumber)) {
        usersByPhoneNumber.remove(ignoreUser);
      }
    }
    boolean emailAlreadyUsed = checkEmail && (userByEmail != null);
    boolean phoneNumberAlreadyUsed = checkPhoneNumber && CollectionUtils.isNotEmpty(usersByPhoneNumber);

    if (emailAlreadyUsed && phoneNumberAlreadyUsed) {
      fraudLog(log, String.format("Already exists user %s with phone %s", email, phoneNumber));
      throw new BadRequestException("This user name and phone number are already in use.");
    }
    if (emailAlreadyUsed) {
      throw new BadRequestException("This email address is already in use");
    }
    if (phoneNumberAlreadyUsed) {
      fraudLog(log, String.format("Already exists user with phone number %s", phoneNumber));
      throw new BadRequestException("This phone number is already in use");
    }
  }

  public User createUser(String email, String socialId, String firstname, String lastname, String password,
    String phonenumber, String userPhoto, Boolean phoneNumberVerified, HttpServletRequest request) throws RideAustinException {
    String deviceId = request.getHeader("User-Device-Id");
    if (deviceId != null && blockedDeviceService.isInBlocklist(deviceId)) {
      fraudLog(log, String.format("Sign up attempt from blocked device %s, email %s, ip %s", deviceId, email, extractIPAddress(request)));
      throw new SignUpException(DEVICE_IS_BLOCKED);
    }

    checkPhoneNumberConstraints(phonenumber);

    User user = userDslRepository.findAnyByEmail(email);
    if (user != null) {
      throw new SignUpException(USER_ALREADY_EXISTS);
    }

    user = new User();
    user.setEmail(email);
    user.setFirstname(firstname);
    user.setLastname(lastname);
    user.setPhoneNumber(phonenumber);
    if (Boolean.FALSE.equals(phoneNumberVerified)) {
      user.setUserEnabled(false);
      user.setPhoneNumberVerified(false);
    } else {
      user.setUserEnabled(true);
    }

    if (!StringUtils.isEmpty(socialId)) {
      user.setFacebookId(socialId);
      user.setPhotoUrl(String.format("https://graph.facebook.com/%s/picture?type=large", socialId));
    }

    // Save photo if passed
    if (!StringUtils.isEmpty(userPhoto)) {
      String fileName = s3StorageService.uploadPublicFile(BaseEntityPhoto.USER_PHOTOS, userPhoto);
      if (fileName != null) {
        user.setPhotoUrl(fileName);
      }
    }

    // Encode the password
    user.setPassword(encoder.encode(password));

    // Set the avatar type
    user.setAvatarTypesBitmask(user.getAvatarTypesBitmask() | AvatarType.RIDER.toBitMask());

    final User persistedUser = userDslRepository.save(user);

    UserTrackData userTrackData = new UserTrackData(request.getParameterMap());
    if (!userTrackData.isEmpty()) {
      userTrackDslRepository.save(new UserTrack(persistedUser, userTrackData));
    }

    return persistedUser;
  }

  private void checkUserRides(User user) throws BadRequestException {
    Rider rider = user.getAvatar(Rider.class);
    if (rider == null) {
      return;
    }
    Collection<Ride> rides = rideRepository.findByRiderAndStatus(rider, RideStatus.REQUESTED);
    if (CollectionUtils.isNotEmpty(rides)) {
      throw new BadRequestException("You cannot logout while you are requesting a ride");
    }
  }
}
