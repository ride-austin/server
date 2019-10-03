package com.rideaustin.service;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.FarePaymentDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.service.farepayment.SplitFareService;
import com.rideaustin.service.notifications.PushNotificationsFacade;

@RunWith(MockitoJUnitRunner.class)
public class SplitFareServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private FarePaymentDslRepository farePaymentDslRepository;
  @Mock
  private RiderDslRepository riderDslRepository;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private PushNotificationsFacade pushNotificationsFacade;


  private SplitFareService splitFareService;

  private Ride ride = new Ride();

  @Before
  public void setup() throws Exception {
    Rider rider = new Rider();
    rider.setId(1L);
    ride = prepareRide(rider);

    splitFareService = new SplitFareService(farePaymentDslRepository, riderDslRepository, rideDslRepository,
      currentUserService, pushNotificationsFacade);
  }

  @Test
  public void testFindPendingSplitFareRequestForRiderWithHimself() throws Exception {
    Rider rider = mockRider(1L);

    when(rideDslRepository.findOne(any())).thenReturn(ride);
    when(currentUserService.getUser()).thenReturn(rider.getUser());
    when(riderDslRepository.findByPhoneNumber(eq("123456"), eq(Boolean.FALSE))).thenReturn(rider);

    expectedException.expect(ForbiddenException.class);
    expectedException.expectMessage("Rider is not allowed to share with himself");
    splitFareService.sendSplitFareRequest(1L, Arrays.asList("123456", "1234"));

  }

  @Test
  public void testFindPendingSplitFareRequestForRider() throws Exception {

    Rider rider = mockRider(2L);
    Ride ride = prepareRide(mockRider(1L));
    when(rideDslRepository.findOne(any())).thenReturn(ride);
    when(currentUserService.getUser()).thenReturn(rider.getUser());
    when(riderDslRepository.findByPhoneNumber(eq("123456"), eq(Boolean.FALSE))).thenReturn(rider);
    when(farePaymentDslRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

    FarePayment farePayment = splitFareService.sendSplitFareRequest(1L, Arrays.asList("123456", "1234"));

    assertNotNull(farePayment);
  }

  private Ride prepareRide(Rider rider) {
    Ride ride = new Ride();
    ride.setId(new Random().nextInt());
    ride.setRequestedCarType(mockCarType());
    ride.setRider(rider);
    ride.setDistanceTravelled(new BigDecimal(1000));
    ride.setStartedOn(new DateTime().minusHours(5).toDate());
    FareDetails fareDetails = FareDetails.builder()
      .distanceFare(Money.of(CurrencyUnit.USD, 1d))
      .totalFare(Money.of(CurrencyUnit.USD, 10d))
      .build();
    ride.setFareDetails(fareDetails);
    ride.setStatus(RideStatus.ACTIVE);
    return ride;
  }

  private CarType mockCarType() {
    return new CarType();
  }

  private Rider mockRider(Long riderId) {
    Rider rider = new Rider();
    rider.setId(riderId);

    User u = new User();
    u.setId(1L);
    u.getAvatarTypes().add(AvatarType.ADMIN);
    rider.setUser(u);
    rider.setActive(true);
    rider.setPrimaryCard(mockRiderCard());
    return rider;
  }

  private RiderCard mockRiderCard() {
    RiderCard rc = new RiderCard();
    rc.setCardBrand(CardBrand.AMERICAN_EXPRESS);
    rc.setCardExpired(false);
    return rc;
  }

}