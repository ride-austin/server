package com.rideaustin.service.notification;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.rideaustin.Constants;
import com.rideaustin.model.RidePushNotificationDTO;
import com.rideaustin.model.RidePushNotificationRepository;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.TokenRepository;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.rideaustin.service.notifications.PushNotificationsService;

@Component
@Profile("!itest")
public class RideFlowPushNotificationFacade extends PushNotificationsFacade {

  private final RidePushNotificationRepository repository;

  @Inject
  public RideFlowPushNotificationFacade(TokenRepository tokenRepository, CityCache cityCache, PushNotificationsService notificationsService,
    Environment environment, RidePushNotificationRepository repository) {
    super(tokenRepository, notificationsService, cityCache, environment);
    this.repository = repository;
  }

  public void sendRideUpdateToRider(Long rideId) {
    RidePushNotificationDTO notificationDTO = repository.get(rideId);
    sendRideUpdateToRider(rideId, notificationDTO.getStatus());
  }

  public void sendRideUpdateToRider(Long rideId, RideStatus overrideStatus) {
    RidePushNotificationDTO notificationDTO = repository.get(rideId);
    String driverName = notificationDTO.getDriverName();
    String msg;
    String eventKey;
    switch (overrideStatus) {
      case NO_AVAILABLE_DRIVER:
        msg = "No available drivers";
        eventKey = notificationDTO.getStatus().name();
        break;
      case DRIVER_ASSIGNED:
        msg = String.format("Your ride request is accepted by %s.", driverName);
        eventKey = notificationDTO.getStatus().name();
        break;
      case DRIVER_CANCELLED:
        msg = createDriverCancellationMessage(notificationDTO, driverName);
        eventKey = notificationDTO.getStatus().name();
        break;
      case ADMIN_CANCELLED:
        msg = "Your ride request has been cancelled by an administrator.";
        eventKey = notificationDTO.getStatus().name();
        break;
      case DRIVER_REACHED:
        msg = String.format("%s has arrived at your location", driverName);
        eventKey = notificationDTO.getStatus().name();
        break;
      case ACTIVE:
        msg = "Your ride is starting.";
        eventKey = "RIDE_ACTIVE";
        break;
      case COMPLETED:
        msg = "Your ride is completed.";
        eventKey = "RIDE_COMPLETED";
        break;
      default:
        msg = "";
        eventKey = "";
        break;
    }

    pushRideNotificationToRider(notificationDTO, msg, eventKey);
  }

  public void pushRideRedispatchNotification(long rideId) {
    RidePushNotificationDTO notificationDTO = repository.get(rideId);
    pushRideNotificationToRider(notificationDTO, "Ride redispatched to next driver", "RIDE_REDISPATCHED");
  }

  private String createDriverCancellationMessage(RidePushNotificationDTO notificationDTO, String driverName) {
    String msg;
    if (notificationDTO.getCancellationFee() != null && notificationDTO.getCancellationFee().isGreaterThan(Constants.ZERO_USD)) {
      msg = String.format("Your ride has been cancelled by %s. You've been charged a %s cancellation fee for no show.", driverName, notificationDTO.getCancellationFee().toString());
    } else {
      msg = String.format("Your ride request is cancelled by %s.", driverName);
    }
    return msg;
  }

  private void pushRideNotificationToRider(RidePushNotificationDTO ride, String msg, String eventKey) {
    if (msg != null && eventKey != null) {
      User user = ride.getUser();
      Map<String, String> dataMap = new HashMap<>();
      dataMap.put(EVENT_KEY, eventKey);
      dataMap.put(ALERT, msg);
      dataMap.put(SOUND, DEFAULT_SOUND);
      dataMap.put("requestId", String.valueOf(ride.getId()));

      pushNotificationToRider(user, dataMap);
    }
  }
}
