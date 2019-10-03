package com.rideaustin.service.user;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.RiderDto;
import com.rideaustin.service.BaseAvatarService;
import com.rideaustin.service.CityService;
import com.rideaustin.service.CurrentSessionService;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.SessionService;
import com.rideaustin.service.UserService;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.model.AvatarUpdateDto;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.thirdparty.StripeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RiderService {

  private static final String RIDER_NOT_FOUND_MESSAGE = "Rider not found";

  private final UserService userService;
  private final EmailService emailService;
  private final CurrentUserService currentUserService;
  private final RiderDslRepository riderDslRepository;
  private final StripeService stripeService;
  private final PromocodeService promocodeService;
  private final CityService cityService;
  private final BaseAvatarService baseAvatarService;
  private final SessionService sessionService;
  private final CurrentSessionService currentSessionService;
  private final BlockedDeviceService blockedDeviceService;

  public Rider updateRider(long id, Rider rider, boolean deviceBlocked, boolean active) throws RideAustinException {
    rider.setActive(!deviceBlocked && active);
    if (deviceBlocked) {
      rider.getUser().setUserEnabled(false);
    }
    Rider current = updateRider(id, rider);
    if (deviceBlocked) {
      blockedDeviceService.addToBlackList(current);
    } else {
      blockedDeviceService.unblock(current);
      if (!current.getUser().getUserEnabled()) {
        current.getUser().setUserEnabled(true);
        current = updateRider(id, current);
      }
    }
    return current;
  }

  private Rider updateRider(long id, Rider newRider) throws RideAustinException {
    Rider current = findRider(id);
    newRider.checkAccess(currentUserService.getUser());
    userService.checkPhoneNumberAvailable(current.getPhoneNumber(), newRider.getPhoneNumber());
    checkConstraintsIfPhoneNumberChanged(newRider, current);
    if (currentUserService.getUser().isAdmin()) {
      baseAvatarService.updateAvatarByAdmin(new AvatarUpdateDto(current), new AvatarUpdateDto(newRider));
      if (current.isActive() != newRider.isActive()) {
        current.getUser().setUserEnabled(newRider.isActive());
        sessionService.endSessionsImmediately(current.getUser());
      }
      current.updateByAdmin(newRider);
    } else {
      current.updateByUser(newRider);
      currentUserService.setUser(newRider.getUser());
    }
    current = riderDslRepository.save(current);
    currentSessionService.refreshUserSession(newRider.getUser(), ApiClientAppType.MOBILE_RIDER);
    return current;
  }

  @Nonnull
  public Rider findRider(long id) throws RideAustinException {
    Rider rider = riderDslRepository.getRiderWithDependencies(id);
    if (rider == null) {
      throw new NotFoundException(RIDER_NOT_FOUND_MESSAGE);
    }
    rider.checkAccess(currentUserService.getUser());
    log.debug("Loaded RIDER's avatars: {}", rider.getUser().getAvatars().size());
    baseAvatarService.enrichAvatarWithLastLoginDate(rider);
    return rider;
  }

  public RiderDto findRiderInfo(long id) throws NotFoundException {
    RiderDto rider = riderDslRepository.findRiderInfo(id);
    if (rider == null) {
      throw new NotFoundException(RIDER_NOT_FOUND_MESSAGE);
    }
    return rider;
  }

  public RiderDto getCurrentRider() throws RideAustinException {
    User currentUser = currentUserService.getUser();
    if (!currentUser.hasAvatar(AvatarType.RIDER)) {
      throw new ForbiddenException("Current user is not a rider");
    }
    RiderDto rider = riderDslRepository.findRiderByUser(currentUser);
    if (rider == null) {
      throw new NotFoundException(RIDER_NOT_FOUND_MESSAGE);
    }
    return rider;
  }

  public Rider createRider(Long cityId, User user) throws RideAustinException {
    Rider rider = new Rider();
    rider.setUser(user);

    user.addAvatar(rider);
    rider.setStripeId(stripeService.createStripeAccount(rider));
    rider.setCityId(cityId);
    rider = riderDslRepository.save(rider);

    // assign unique promocode to rider
    promocodeService.assignRiderPromocode(rider);
    // Send out the signup email
    try {
      emailService.sendEmail(new RiderSignUpEmail(rider, cityService.getById(cityId)));
    } catch (EmailException e) {
      log.error("Failed to send signup email", e);
    }
    return rider;
  }

  private void checkConstraintsIfPhoneNumberChanged(Rider updatedRider, Rider currentRider) throws RideAustinException {
    final String updatedPhoneNumber = updatedRider.getPhoneNumber();
    final String currentPhoneNumber = currentRider.getPhoneNumber();
    if (!StringUtils.equals(updatedPhoneNumber, currentPhoneNumber) && !currentRider.getUser().isDriver()) {
      userService.checkPhoneNumberConstraints(updatedPhoneNumber);
    }
  }
}