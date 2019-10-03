package com.rideaustin.service.ride;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.service.CurrentUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideOwnerService {

  private final CurrentUserService currentUserService;
  private final UserDslRepository userDslRepository;
  private final RideDslRepository rideDslRepository;

  public boolean isRideRider(Long rideId) {
    User user = currentUserService.getUser();
    return checkRiderOwnership(rideId, user);
  }

  public boolean isRideRider(Long userId, Long rideId) {
    User user = userDslRepository.findOne(userId);
    return checkRiderOwnership(rideId, user);
  }

  public boolean isDriversRide(Long rideId) {
    User user = currentUserService.getUser();
    return checkDriverOwnership(rideId, user);
  }

  private boolean checkRiderOwnership(Long rideId, User user) {
    User riderUser = rideDslRepository.findRiderUser(rideId);
    if (riderUser == null) {
      return false;
    }
    return user.isRider() && riderUser.equals(user);
  }

  private boolean checkDriverOwnership(Long rideId, User user) {
    User driverUser = rideDslRepository.findDriverUser(rideId);
    if (driverUser == null) {
      return false;
    }
    return user.isDriver() && user.equals(driverUser);
  }

}
