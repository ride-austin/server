package com.rideaustin.service.rating;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RiderDslRepository;

public class RiderRatingServiceImplTest {

  private RiderRatingService testedInstance;

  @Mock
  private RiderDslRepository riderDslRepository;
  @Mock
  private ConfigurationItemCache configurationItemCache;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new RiderRatingServiceImpl(configurationItemCache, riderDslRepository);

    when(riderDslRepository.getRiderRatingCount(any())).thenReturn(10L);
    when(riderDslRepository.findRatingAverage(any(), any(), any(), any())).thenReturn(4d);
    when(configurationItemCache.getConfigurationForClient(any())).thenReturn(this.buildConfigurationItems());
  }

  private List<ConfigurationItem> buildConfigurationItems() {
    ImmutableMap<Object, Object> config = ImmutableMap.of(
      AbstractRatingService.MINIMUM_RATING_THRESHOLD_KEY, 10,
      AbstractRatingService.DEFAULT_RATING_KEY, 5d,
      AbstractRatingService.LIMIT_KEY, 50);
    return ImmutableList.of(
      ConfigurationItem.builder()
        .clientType(ClientType.RIDER)
        .configurationKey(AbstractRatingService.RATING_CONFIG)
        .configurationObject(
          config
        )
        .build());
  }

  @Test
  public void updateRiderRatingFromConfig() {
    Rider r = new Rider();
    testedInstance.updateRiderRating(r);
    assertThat(r.getRating(), is(4d));
  }

  @Test
  public void updateRiderRatingTestFromDB() {
    Rider r = new Rider();
    when(riderDslRepository.getRiderRatingCount(any())).thenReturn(5L);
    testedInstance.updateRiderRating(r);
    assertThat(r.getRating(), is(4d));
  }

}