package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.CancellationReasonDto;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CancellationReason;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideCancellationFeedback;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.CancellationFeedbackDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.service.ride.RideOwnerService;

public class CancellationFeedbackServiceTest {

  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private RideOwnerService rideOwnerService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private CancellationFeedbackDslRepository repository;
  @Mock
  private ConfigurationItemCache configurationItemCache;
  private ObjectMapper mapper = new ObjectMapper();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private CancellationFeedbackService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CancellationFeedbackService(currentUserService, rideOwnerService, rideDslRepository, repository,
      configurationItemCache, mapper);
  }

  @Test
  public void submitThrowsBadRequestExceptionOnNonExistentRide() throws BadRequestException {
    long rideId = 1L;
    when(rideDslRepository.findOne(rideId)).thenReturn(null);

    expectedException.expectMessage("Ride is not cancelled or doesn't exist");

    testedInstance.submit(rideId, CancellationReason.CHANGE_MIND, AvatarType.RIDER, "A");
  }

  @Test
  public void submitThrowsBadRequestExceptionOnNotCancelledRide() throws BadRequestException {
    long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.ACTIVE);
    when(rideDslRepository.findOne(rideId)).thenReturn(ride);

    expectedException.expectMessage("Ride is not cancelled or doesn't exist");

    testedInstance.submit(rideId, CancellationReason.CHANGE_MIND, AvatarType.RIDER, "A");
  }

  @Test
  public void submitThrowsBadRequestExceptionOnAnotherRidersRide() throws BadRequestException {
    long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.RIDER_CANCELLED);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    when(rideOwnerService.isRideRider(eq(rideId))).thenReturn(false);

    expectedException.expectMessage("You can't post feedback to this ride");

    testedInstance.submit(rideId, CancellationReason.CHANGE_MIND, AvatarType.RIDER, "A");
  }

  @Test
  public void submitThrowsBadRequestExceptionOnAnotherDriversRide() throws BadRequestException {
    long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.RIDER_CANCELLED);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    when(rideOwnerService.isDriversRide(eq(rideId))).thenReturn(false);

    expectedException.expectMessage("You can't post feedback to this ride");

    testedInstance.submit(rideId, CancellationReason.NO_SHOW, AvatarType.DRIVER, "A");
  }

  @Test
  public void submitThrowsBadRequestExceptionOnAlreadySubmittedFeedback() throws BadRequestException {
    long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.RIDER_CANCELLED);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    when(rideOwnerService.isRideRider(eq(rideId))).thenReturn(true);
    final long userId = 1L;
    final User user = new User();
    user.setId(userId);
    when(currentUserService.getUser()).thenReturn(user);
    when(repository.noFeedbackYet(eq(rideId), eq(userId))).thenReturn(false);

    expectedException.expectMessage("Feedback has been already submitted!");

    testedInstance.submit(rideId, CancellationReason.CHANGE_MIND, AvatarType.RIDER, "A");
  }

  @Test
  public void submitSavesFeedback() throws BadRequestException {
    long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.RIDER_CANCELLED);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    when(rideOwnerService.isRideRider(eq(rideId))).thenReturn(true);
    final long userId = 1L;
    final User user = new User();
    user.setId(userId);
    when(currentUserService.getUser()).thenReturn(user);
    when(repository.noFeedbackYet(eq(rideId), eq(userId))).thenReturn(true);


    testedInstance.submit(rideId, CancellationReason.CHANGE_MIND, AvatarType.RIDER, "A");

    verify(repository, times(1)).save(argThat(new BaseMatcher<RideCancellationFeedback>() {
      @Override
      public boolean matches(Object o) {
        final RideCancellationFeedback cancellationFeedback = (RideCancellationFeedback) o;
        return cancellationFeedback.getComment().equals("A") &&
          cancellationFeedback.getReason().equals(CancellationReason.CHANGE_MIND) &&
          cancellationFeedback.getRideId() == rideId &&
          cancellationFeedback.getSubmittedBy() == userId;
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }

  @Test
  public void listReasonsReturnsFallbackOnEmptyConfiguration() {
    when(configurationItemCache.getConfigurationForClient(eq(ClientType.CONSOLE), anyString(), anyLong()))
      .thenReturn(Optional.empty());

    final List<CancellationReasonDto> result = testedInstance.listReasons(1L, AvatarType.RIDER);

    assertReasonsFallback(result);
  }

  @Test
  public void listReasonsReturnsFallbackOnWrongConfiguration() {
    final ConfigurationItem config = new ConfigurationItem();
    config.setConfigurationValue("{");
    when(configurationItemCache.getConfigurationForClient(eq(ClientType.CONSOLE), anyString(), anyLong()))
      .thenReturn(Optional.of(config));

    final List<CancellationReasonDto> result = testedInstance.listReasons(1L, AvatarType.RIDER);

    assertReasonsFallback(result);
  }

  @Test
  public void listReasonsReturnsConfiguredList() {
    final ConfigurationItem config = new ConfigurationItem();
    config.setConfigurationValue("{\"RIDER\":{\"CHANGE_BOOKING\":\"I needed to change booking (time/place)\"}}");
    when(configurationItemCache.getConfigurationForClient(eq(ClientType.CONSOLE), anyString(), anyLong()))
      .thenReturn(Optional.of(config));

    final List<CancellationReasonDto> result = testedInstance.listReasons(1L, AvatarType.RIDER);

    assertEquals(1, result.size());
    assertEquals(CancellationReason.CHANGE_BOOKING, result.get(0).getCode());
    assertEquals("I needed to change booking (time/place)", result.get(0).getDescription());
  }

  private void assertReasonsFallback(List<CancellationReasonDto> result) {
    assertFalse(result.isEmpty());
    assertEquals(CancellationReason.values().length, result.size());
    assertTrue(CollectionUtils.isEqualCollection(result.stream().map(CancellationReasonDto::getCode).collect(Collectors.toSet()),
      EnumSet.allOf(CancellationReason.class)));
  }
}