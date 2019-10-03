package com.rideaustin.assemblers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.Session;
import com.rideaustin.rest.model.ActiveDriverDto;
import com.rideaustin.service.CurrentSessionService;
import com.rideaustin.service.user.CarTypesCache;

public class ActiveDriverDtoEnricherTest {

  private ActiveDriverDtoEnricher testedInstance;
  @Mock
  private CurrentSessionService currentSessionService;
  @Mock
  private CarTypesCache carTypesCache;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new ActiveDriverDtoEnricher(currentSessionService, carTypesCache);
  }

  @Test
  public void enrichSetsCarCategories() {
    final ActiveDriverDto source = createObjectToEnrich();

    final ActiveDriverDto result = testedInstance.enrich(source);

    assertThat(result.getCarCategories()).containsExactly("REGULAR");
  }

  @Test
  public void enrichSetsAppVersion() {
    final ActiveDriverDto source = createObjectToEnrich();
    final Session session = new Session();
    session.setUserAgent("RideAustin_iOS_5.1.0 (707)");
    when(currentSessionService.getCurrentSession(eq(source.getUserId()))).thenReturn(session);

    final ActiveDriverDto result = testedInstance.enrich(source);

    assertEquals("iOS 5.1.0 (707)", result.getAppVersion());
  }

  private ActiveDriverDto createObjectToEnrich() {
    double latitude = 34.546;
    double longitude = -97.546;
    int availableCarCategories = 1;
    long driverId = 1L;
    String fullName = "A B";
    String phoneNumber = "+15555555555";
    long userId = 1L;
    final ActiveDriverDto source = new ActiveDriverDto(latitude, longitude, availableCarCategories, driverId, fullName,
      phoneNumber, userId);

    when(carTypesCache.fromBitMask(anyInt())).thenReturn(Collections.singleton("REGULAR"));
    return source;
  }
}