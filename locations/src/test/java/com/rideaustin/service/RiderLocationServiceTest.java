package com.rideaustin.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.location.model.RiderLocation;

public class RiderLocationServiceTest {

  public static final long RIDER_ID = 1L;
  private RiderLocationService testedInstance;

  @Mock
  private EventsNotificationService eventsNotificationService;
  @Mock
  private Environment environment;
  @Mock
  private ConfigurationItemCache configCache;
  @Mock
  private RedisTemplate redisTemplate;
  private Ride ride;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(environment.getProperty(eq("rider.location.update.threshold"), eq(Integer.class), anyInt())).thenReturn(5000);
    testedInstance = new RiderLocationService(redisTemplate, environment, eventsNotificationService, configCache);

    ride = new Ride();
    Rider rider = new Rider();
    rider.setId(RIDER_ID);
    ride.setRider(rider);
    ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setDriver(new Driver());
    ride.setActiveDriver(activeDriver);
  }

  @Test
  public void testProcessLocationUpdateCreatesNewLocationObjectIfRiderOnlyAppeared() throws Exception {
    setupConfig(true);

    double latitude = 0.0;
    double longitude = 0.0;
    testedInstance.processLocationUpdate(ride.getRider().getId(), ride.getActiveDriver().getDriver().getId(), latitude, longitude);

    verify(eventsNotificationService, only()).sendRiderLocationUpdate(anyLong(), eq(latitude), eq(longitude), anyLong());
  }

  @Test
  public void testProcessLocationSendsUpdateIfLastLocationUpdateWasBeforeThreshold() {
    setupConfig(true);
    LocationObject locationObject = new LocationObject();
    locationObject.setLocationUpdateDate(Date.from(Instant.now().minus(10, ChronoUnit.SECONDS)));

    double latitude = 0.0;
    double longitude = 0.0;
    testedInstance.processLocationUpdate(ride.getRider().getId(), ride.getActiveDriver().getDriver().getId(), latitude, longitude);

    verify(eventsNotificationService, only()).sendRiderLocationUpdate(anyLong(), eq(latitude), eq(longitude), anyLong());
  }

  @Test
  public void testProcessLocationDoesntSendUpdateIfLastLocationUpdateWasWithinThreshold() {
    setupConfig(true);
    double latitude = 0.0;
    double longitude = 0.0;
    RiderLocation riderLocation = new RiderLocation(ride.getRider().getId(), latitude, longitude);
    riderLocation.setLocationUpdateDate(Date.from(Instant.now().minus(1, ChronoUnit.SECONDS)));
    when(redisTemplate.execute(any(SessionCallback.class))).thenReturn(riderLocation);

    testedInstance.processLocationUpdate(ride.getRider().getId(), ride.getActiveDriver().getDriver().getId(), latitude, longitude);

    verify(eventsNotificationService, never()).sendRiderLocationUpdate(anyLong(), eq(latitude), eq(longitude), anyLong());
  }

  private void setupConfig(boolean enabled) {
    ConfigurationItem config = new ConfigurationItem();
    config.setConfigurationKey("riderLiveLocation");
    config.setConfigurationObject(ImmutableMap.of("enabled", enabled));
    when(configCache.getConfigurationForClient(ClientType.RIDER)).thenReturn(Collections.singletonList(config));
  }


}