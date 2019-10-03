package com.rideaustin.service.ride;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.service.CurrentUserService;

public class RideOwnerServiceTest {

  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private UserDslRepository userDslRepository;
  @Mock
  private RideDslRepository rideDslRepository;

  private RideOwnerService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new RideOwnerService(currentUserService, userDslRepository, rideDslRepository);
  }

  @Test
  public void isRideRiderCurrentUserReturnsFalseWhenUserNotFound() {
    final long rideId = 1L;
    when(rideDslRepository.findRiderUser(rideId)).thenReturn(null);

    final boolean result = testedInstance.isRideRider(rideId);

    assertFalse(result);
  }

  @Test
  public void isRideRiderCurrentUserReturnsFalseWhenCurrentUserIsNotRider() {
    final long rideId = 1L;
    when(rideDslRepository.findRiderUser(rideId)).thenReturn(new User());
    when(currentUserService.getUser()).thenReturn(new User());

    final boolean result = testedInstance.isRideRider(rideId);

    assertFalse(result);
  }

  @Test
  public void isRideRiderCurrentUserReturnsFalseWhenRideUserIsNotCurrent() {
    final long rideId = 1L;
    when(rideDslRepository.findRiderUser(rideId)).thenReturn(new User());
    final User currentUser = new User();
    currentUser.addAvatar(new Rider());
    currentUser.setId(1L);
    when(currentUserService.getUser()).thenReturn(currentUser);

    final boolean result = testedInstance.isRideRider(rideId);

    assertFalse(result);
  }

  @Test
  public void isRideRiderCurrentUserReturnsTrueWhenRideBelongsToCurrentUser() {
    final long rideId = 1L;
    final long userId = 1L;
    final User rideUser = new User();
    rideUser.setId(userId);
    when(rideDslRepository.findRiderUser(rideId)).thenReturn(rideUser);
    final User currentUser = new User();
    currentUser.addAvatar(new Rider());
    currentUser.setId(userId);
    when(currentUserService.getUser()).thenReturn(currentUser);

    final boolean result = testedInstance.isRideRider(rideId);

    assertTrue(result);
  }

  @Test
  public void isRideRiderGivenUserReturnsFalseWhenUserNotFound() {
    final long rideId = 1L;
    final long userId = 1L;
    when(rideDslRepository.findRiderUser(rideId)).thenReturn(null);

    final boolean result = testedInstance.isRideRider(userId, rideId);

    assertFalse(result);
  }

  @Test
  public void isRideRiderGivenUserReturnsFalseWhenGivenUserIsNotRider() {
    final long rideId = 1L;
    final long userId = 1L;
    when(userDslRepository.findOne(eq(userId))).thenReturn(new User());
    when(rideDslRepository.findRiderUser(rideId)).thenReturn(new User());

    final boolean result = testedInstance.isRideRider(userId, rideId);

    assertFalse(result);
  }

  @Test
  public void isRideRiderGivenUserReturnsFalseWhenRideUserIsNotGiven() {
    final long rideId = 1L;
    final long userId = 1L;
    final User givenUser = new User();
    givenUser.setId(1L);
    givenUser.addAvatar(new Rider());
    when(userDslRepository.findOne(eq(userId))).thenReturn(givenUser);
    when(rideDslRepository.findRiderUser(rideId)).thenReturn(new User());

    final boolean result = testedInstance.isRideRider(userId, rideId);

    assertFalse(result);
  }

  @Test
  public void isRideRiderGivenUserReturnsTrueWhenRideBelongsToGivenUser() {
    final long rideId = 1L;
    final long userId = 1L;
    final User givenUser = new User();
    givenUser.setId(userId);
    givenUser.addAvatar(new Rider());
    when(userDslRepository.findOne(eq(userId))).thenReturn(givenUser);
    final User riderUser = new User();
    riderUser.setId(userId);
    when(rideDslRepository.findRiderUser(rideId)).thenReturn(riderUser);

    final boolean result = testedInstance.isRideRider(userId, rideId);

    assertTrue(result);
  }

  @Test
  public void isDriversRideReturnsFalseWhenUserNotFound() {
    final long rideId = 1L;
    when(rideDslRepository.findDriverUser(rideId)).thenReturn(null);

    final boolean result = testedInstance.isDriversRide(rideId);

    assertFalse(result);
  }

  @Test
  public void isDriversRideReturnsFalseWhenCurrentUserIsNotDriver() {
    final long rideId = 1L;
    when(rideDslRepository.findDriverUser(rideId)).thenReturn(new User());
    when(currentUserService.getUser()).thenReturn(new User());

    final boolean result = testedInstance.isDriversRide(rideId);

    assertFalse(result);
  }

  @Test
  public void isDriversRideReturnsFalseWhenRideUserIsNotCurrent() {
    final long rideId = 1L;
    when(rideDslRepository.findDriverUser(rideId)).thenReturn(new User());
    final User currentUser = new User();
    currentUser.addAvatar(new Driver());
    currentUser.setId(1L);
    when(currentUserService.getUser()).thenReturn(currentUser);

    final boolean result = testedInstance.isDriversRide(rideId);

    assertFalse(result);
  }

  @Test
  public void isDriversRideReturnsTrueWhenRideBelongsToCurrentUser() {
    final long rideId = 1L;
    final long userId = 1L;
    final User rideUser = new User();
    rideUser.setId(userId);
    when(rideDslRepository.findDriverUser(rideId)).thenReturn(rideUser);
    final User currentUser = new User();
    currentUser.addAvatar(new Driver());
    currentUser.setId(userId);
    when(currentUserService.getUser()).thenReturn(currentUser);

    final boolean result = testedInstance.isDriversRide(rideId);

    assertTrue(result);
  }
}