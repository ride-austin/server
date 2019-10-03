package com.rideaustin.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.notifications.PushNotificationsFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TipReminderService {

  private final RideDslRepository rideDslRepository;
  private final PushNotificationsFacade pushNotificationsFacade;

  public void sendTipReminderToRider(Long rideId) {
    Ride ride = rideDslRepository.findOne(rideId);
    if (ride != null && ride.getDriverRating() == null) {
      User riderUser = rideDslRepository.findRiderUser(rideId);
      pushNotificationsFacade.pushTipReminderNotificationToRider(ride, riderUser);
    }
  }

}
