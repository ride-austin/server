package com.rideaustin.service.rating;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DriverDslRepository;

public class DriverRatingServiceImplTest {

  @Mock
  private DriverDslRepository driverDslRepository;
  @Mock
  private ConfigurationItemCache configurationItemCache;

  private DriverRatingServiceImpl testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new DriverRatingServiceImpl(configurationItemCache, driverDslRepository);
    when(configurationItemCache.getConfigurationForClient(any())).thenReturn(this.buildConfigurationItems());

    List<Ride> mockedRides = mockRides(45, 5.0);
    mockedRides.addAll(mockRides(5, 0.0));
    when(driverDslRepository.getDriverRatingCount(any())).thenReturn(10L);
    when(driverDslRepository.getDriverRatedRides(any(), anyInt())).thenReturn(mockedRides);

  }

  @Test
  public void testUpdateDriverRatingFromConfig() {
    when(driverDslRepository.findRatingAverage(any(), any(), any(), any())).thenReturn(4.5d);
    Driver r = new Driver();
    testedInstance.updateDriverRating(r);
    assertThat(r.getRating(), is(4.5d));
  }

  @Test
  public void testUpdateDriverRatingTestFromDB() {
    Driver r = new Driver();
    when(driverDslRepository.findRatingAverage(any(), any(), any(), any())).thenReturn(5d);
    testedInstance.updateDriverRating(r);
    assertThat(r.getRating(), is(5d));
  }

  private List<ConfigurationItem> buildConfigurationItems() {
    ImmutableMap<Object, Object> config = ImmutableMap.of(
      AbstractRatingService.MINIMUM_RATING_THRESHOLD_KEY, 10,
      AbstractRatingService.DEFAULT_RATING_KEY, 5d,
      AbstractRatingService.LIMIT_KEY, 50);
    return ImmutableList.of(
      ConfigurationItem.builder()
        .clientType(ClientType.DRIVER)
        .configurationKey(AbstractRatingService.RATING_CONFIG)
        .configurationObject(
          config
        )
        .build());
  }

  private List<Ride> mockRides(int n, double rating) {
    List<Ride> rides = Lists.newArrayList();
    for (int i = 0; i < n; i++) {
      Ride r = new Ride();
      r.setDriverRating(rating);
      rides.add(r);
    }
    return rides;
  }
}