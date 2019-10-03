package com.rideaustin.service.config;

import static com.rideaustin.service.config.RideAcceptanceConfig.ACCEPTANCE_PERIOD;
import static com.rideaustin.service.config.RideAcceptanceConfig.ALLOWANCE_PERIOD;
import static com.rideaustin.service.config.RideAcceptanceConfig.LATENCY_COVERAGE;
import static com.rideaustin.service.config.RideAcceptanceConfig.RIDE_ACCEPTANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;

public class RideAcceptanceConfigTest {

  private RideAcceptanceConfig rideAcceptanceConfig;

  @Mock
  private ConfigurationItemCache configurationItemCache;

  private static final int DEFAULT_ACCEPTANCE = 10;

  private static final int DEFAULT_ALLOWANCE = 5;

  private static final int DEFAULT_LATENCY_COVERAGE = 2;

  private static final Long DEFAULT_CITY = 1L;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(configurationItemCache.getConfigAsInt(eq(ClientType.DRIVER), eq(RIDE_ACCEPTANCE), eq(ACCEPTANCE_PERIOD), eq(DEFAULT_CITY)))
      .thenReturn(DEFAULT_ACCEPTANCE);
    when(configurationItemCache.getConfigAsInt(eq(ClientType.DRIVER), eq(RIDE_ACCEPTANCE), eq(ALLOWANCE_PERIOD), eq(DEFAULT_CITY)))
      .thenReturn(DEFAULT_ALLOWANCE);
    when(configurationItemCache.getConfigAsInt(eq(ClientType.DRIVER), eq(RIDE_ACCEPTANCE), eq(LATENCY_COVERAGE), eq(DEFAULT_CITY)))
      .thenReturn(DEFAULT_LATENCY_COVERAGE);
    rideAcceptanceConfig = new RideAcceptanceConfig(configurationItemCache);
  }

  @Test
  public void shouldReturnDriverAcceptancePeriod() {
    int actual = rideAcceptanceConfig.getDriverAcceptancePeriod(DEFAULT_CITY);

    assertThat(actual).isEqualTo(DEFAULT_ACCEPTANCE);
  }

  @Test
  public void shouldReturnServerAllowancePeriod() {
    int actual = rideAcceptanceConfig.getAllowancePeriod(DEFAULT_CITY);

    assertThat(actual).isEqualTo(DEFAULT_ALLOWANCE);
  }

  @Test
  public void shouldReturnPerDriverWaitPeriod() {
    int actual = rideAcceptanceConfig.getPerDriverWaitPeriod(DEFAULT_CITY);

    assertThat(actual).isEqualTo(DEFAULT_ACCEPTANCE + DEFAULT_LATENCY_COVERAGE);
  }
}
