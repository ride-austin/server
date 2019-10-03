package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.Address;
import com.rideaustin.model.GeolocationLog;
import com.rideaustin.model.enums.GeolocationLogEvent;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.GeolocationLogDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.RideAustinException;

@RunWith(MockitoJUnitRunner.class)
public class GeolocationLogServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private GeolocationLogDslRepository geolocationLogDslRepository;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RiderDslRepository riderDslRepository;

  private GeolocationLogService geolocationLogService;

  private Rider rider;

  @Before
  public void setup() throws Exception {
    rider = new Rider();
    User user = new User();
    rider.setUser(user);
    rider.getUser().setId(new Random().nextLong());
    Mockito.when(currentUserService.getUser()).thenReturn(user);


    geolocationLogService = new GeolocationLogService(geolocationLogDslRepository, currentUserService,
      rideDslRepository, riderDslRepository);
  }

  @Test
  public void testSaveLogInvalidUser() throws RideAustinException {
    Rider otherRider = new Rider();
    otherRider.setId(new Random().nextLong());
    User otherUser = new User();
    otherRider.setId(new Random().nextLong());
    otherRider.setUser(otherUser);
    when(riderDslRepository.getRider(otherRider.getId())).thenReturn(otherRider);

    expectedException.expect(ForbiddenException.class);
    geolocationLogService.addGeolocationLog(0d, 0d, GeolocationLogEvent.RIDER_APP_OPEN, otherRider.getId());
  }

  @Test
  public void testSaveLog() throws Exception {
    double lat = new Random().nextDouble();
    double lng = new Random().nextDouble();

    when(riderDslRepository.getRider(rider.getId())).thenReturn(rider);
    when(geolocationLogDslRepository.save(any(GeolocationLog.class))).then(a -> a.getArguments()[0]);

    GeolocationLog log = geolocationLogService.addGeolocationLog(lat, lng, GeolocationLogEvent.RIDER_APP_OPEN, rider.getId());

    assertThat(log.getLocationLat(), is(lat));
    assertThat(log.getLocationLng(), is(lng));
    assertThat(log.getRider(), is(rider));
    assertThat(log.getEvent(), is(GeolocationLogEvent.RIDER_APP_OPEN));
  }

  @Test
  public void testSaveLogOnARide() throws Exception {
    double lat = new Random().nextDouble();
    double lng = new Random().nextDouble();

    Address address = new Address();
    address.setAddress("USA, Austin");
    address.setZipCode("AAA");
    when(riderDslRepository.getRider(rider.getId())).thenReturn(rider);
    when(rideDslRepository.isInRide(rider)).thenReturn(true);

    geolocationLogService.addGeolocationLog(lat, lng, GeolocationLogEvent.RIDER_APP_OPEN, rider.getId());

    verify(geolocationLogDslRepository, never()).save(any(GeolocationLog.class));
  }

  @Test
  public void testGetBetweenDates() throws Exception {
    GeolocationLog log = createLog((long) 1L);
    when(geolocationLogDslRepository.findBetweenDatesWithEvent(any(), any(), any()))
      .thenReturn(Collections.singletonList(log));

    List<GeolocationLog> logs = geolocationLogService.findBetweenDates(new Date(), new Date());
    assertThat(logs.size(), is(1));
  }

  @Test
  public void testGetBetweenDatesDistinctsRiders() throws Exception {
    when(geolocationLogDslRepository.findBetweenDatesWithEvent(any(), any(), any()))
      .thenReturn(ImmutableList.of(createLog(3L, 1L), createLog(2L, 1L), createLog(1L, 2L)));

    List<GeolocationLog> logs = geolocationLogService.findBetweenDates(new Date(), new Date());
    assertThat(logs.size(), is(2));
    assertEquals(1, logs.get(0).getId());
  }

  private GeolocationLog createLog(long riderId) {
    return createLog(1L, riderId);
  }

  private GeolocationLog createLog(long id, long riderId) {
    GeolocationLog log = new GeolocationLog();
    Rider rider = new Rider();
    rider.setId(riderId);
    log.setRider(rider);
    log.setId(id);
    CarType ct = new CarType();
    ct.setCarCategory("REGULAR");
    log.setCarType(ct);
    return log;
  }

}
