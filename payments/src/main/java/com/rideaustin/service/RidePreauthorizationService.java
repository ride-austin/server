package com.rideaustin.service;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.rideaustin.model.ride.CarType.Configuration;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.thirdparty.StripeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RidePreauthorizationService {

  private final StripeService stripeService;

  public String preauthorizeRide(Ride ride, Configuration carTypeConfig, final String applePayToken, final Runnable asyncAction) throws RideAustinException {
    String preChargeId = null;
    if (!carTypeConfig.isSkipRideAuthorization()) {
      log.info(String.format("[Ride #%d][PREAUTH] Running async action", ride.getId()));
      asyncAction.run();
      log.info(String.format("[Ride #%d][PREAUTH] Async action completed", ride.getId()));
      if (StringUtils.isBlank(applePayToken)) {
        preChargeId = stripeService.authorizeRide(ride, ride.getRider().getPrimaryCard());
      } else {
        preChargeId = stripeService.authorizeRide(ride, applePayToken);
      }
      log.info(String.format("[Ride #%d][PREAUTH] Preauthorization charge %s", ride.getId(), preChargeId));
    } else {
      log.info(String.format("[Ride #%d][PREAUTH] Preauthorization is disabled for %s rides",
        ride.getId(), ride.getRequestedCarType().getCarCategory()));
    }
    return preChargeId;
  }
}
