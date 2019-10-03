package com.rideaustin.service.event;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.clients.configuration.events.ConfigurationItemCreatedEvent;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.repo.dsl.DriverDslRepository;

public class ConfigurationItemCreatedEventListenerTest {

  @Mock
  private EventsNotificationService eventsNotificationService;
  @Mock
  private DriverDslRepository driverDslRepository;

  private ConfigurationItemCreatedEventListener testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ConfigurationItemCreatedEventListener(eventsNotificationService, driverDslRepository);
  }

  @Test
  public void handleSkipsNotificationForRiders() {
    final ConfigurationItemCreatedEvent event = new ConfigurationItemCreatedEvent(
      this, 1L, "A", ClientType.RIDER, "A"
    );

    testedInstance.handle(event);

    verify(eventsNotificationService, never()).sendConfigurationItemCreatedEvent(
      anyLong(), eq(AvatarType.RIDER), anyLong(), anyString(), anyString()
    );
  }

  @Test
  public void handleSendsNotificationForDrivers() {
    final long driverId = 1L;
    final long configurationItemId = 1L;
    final String configurationKey = "A";
    final String value = "B";
    final ConfigurationItemCreatedEvent event = new ConfigurationItemCreatedEvent(
      this, configurationItemId, configurationKey, ClientType.DRIVER, value
    );
    when(driverDslRepository.findDriverIds(any())).thenReturn(Collections.singletonList(driverId));

    testedInstance.handle(event);

    verify(eventsNotificationService).sendConfigurationItemCreatedEvent(
      eq(driverId), eq(AvatarType.DRIVER), eq(configurationItemId), eq(configurationKey), eq(value)
    );
  }
}