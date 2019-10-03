package com.rideaustin.dispatch.tasks;

import java.util.Collections;

import javax.inject.Inject;

import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.dispatch.service.RideFlowStateMachineProvider;
import com.rideaustin.model.ride.CarType.Configuration;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.RidePreauthorizationService;
import com.rideaustin.service.model.Events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PreauthorizationTask implements Runnable {

  private final ObjectMapper mapper;
  private final RideDslRepository rideDslRepository;
  private final RidePreauthorizationService preauthorizationService;
  private final RideFlowStateMachineProvider machineProvider;
  private final RedissonClient redissonClient;

  private String applePayToken;
  private Long rideId;

  @Override
  public void run() {
    if (rideId == null) {
      log.error("[PREAUTH] Failed to start preauth task, ride ID is not set");
      return;
    }
    log.info("[PREAUTH] Starting preauth");
    final Ride ride = rideDslRepository.findOneWithRider(rideId);
    if (ride.getPreChargeId() != null) {
      log.info("[PREAUTH] Abort preauth, ride is already authorized");
      return;
    }
    Configuration carTypeConfig = ride.getRequestedCarType().getConfigurationObject(mapper, Configuration.class);
    final RSemaphore semaphore = redissonClient.getSemaphore(String.format("ride:%d:preauth", rideId));
    semaphore.trySetPermits(1);
    try {
      final String preChargeId = preauthorizationService.preauthorizeRide(ride, carTypeConfig, applePayToken, () -> semaphore.tryAcquire(1));
      rideDslRepository.setPrechargeId(rideId, preChargeId);
    } catch (RideAustinException e) {
      log.error("Failed to precharge", e);
      machineProvider.sendProxiedEvent(rideId, Events.ABORT_PREAUTHORIZATION_FAILED, new MessageHeaders(Collections.emptyMap()));
    } finally {
      semaphore.release(1);
    }
  }

  public PreauthorizationTask withApplePayToken(String applePayToken) {
    this.applePayToken = applePayToken;
    return this;
  }

  public PreauthorizationTask withRideId(long id) {
    this.rideId = id;
    return this;
  }
}
