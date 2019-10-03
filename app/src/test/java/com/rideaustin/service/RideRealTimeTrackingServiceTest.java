package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import com.rideaustin.model.City;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.ride.RideOwnerService;
import com.rideaustin.utils.RandomString;

@RunWith(MockitoJUnitRunner.class)
public class RideRealTimeTrackingServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private Environment environment;
  @Mock
  private EmailService emailService;
  @Mock
  private RideOwnerService rideOwnerService;
  @Mock
  private CityService cityService;
  @Mock
  private RideTrackerService rideTrackerService;

  private RideRealTimeTrackingService rideRealTimeTrackingService;

  private Ride ride;

  @Before
  public void setup() throws Exception {
    ride = new Ride();
    ride.setId(1L);
    User cu = new User();
    Set<AvatarType> avatarTypes = new HashSet<>();
    avatarTypes.add(AvatarType.ADMIN);
    cu.setAvatarTypes(avatarTypes);
    when(rideDslRepository.findOne(any(Long.class))).thenReturn(ride);
    when(currentUserService.getUser()).thenReturn(cu);
    when(environment.getProperty(anyString(), anyString())).thenReturn("http://test/");
    when(cityService.getById(any())).thenReturn(createCity());
    rideRealTimeTrackingService = new RideRealTimeTrackingService(rideDslRepository, currentUserService,
      rideOwnerService, rideTrackerService, environment, emailService, cityService);
  }

  @Test(expected = NotFoundException.class)
  public void testGetCurrentRideTrackingForNonExistentRide() throws NotFoundException {
    String trackingKey = RandomString.generate();

    when(rideDslRepository.getRideForTrackingShareToken(trackingKey)).thenReturn(null);

    rideRealTimeTrackingService.getRideForTrackingShareToken(trackingKey);
  }

  @Test
  public void testShareRideToFollow() throws Exception {
    when(cityService.getById(anyLong())).thenReturn(createCity());
    rideRealTimeTrackingService.shareRideToFollow(1L, "test@test.us");
    assertThat(ride.getTrackingShareToken(), is(notNullValue()));
  }

  @Test
  public void testShareRideToFollowBadUser() throws Exception {
    when(currentUserService.getUser()).thenReturn(new User());
    expectedException.expect(ForbiddenException.class);
    rideRealTimeTrackingService.shareRideToFollow(1L, "test@test.us");
  }

  @Test
  public void testGetShareToken() throws Exception {
    rideRealTimeTrackingService.getShareToken(1L);
    assertThat(ride.getTrackingShareToken(), is(notNullValue()));
  }

  @Test
  public void testGetShareTokenBadUser() throws Exception {
    when(currentUserService.getUser()).thenReturn(new User());
    expectedException.expect(ForbiddenException.class);
    rideRealTimeTrackingService.getShareToken(1L);
  }

  private City createCity() {
    City city = new City();
    city.setPageUrl("www.test.page");
    city.setContactEmail("city@mail.com");
    return city;
  }
}
