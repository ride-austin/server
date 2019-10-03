package com.rideaustin.service;

import static com.rideaustin.service.FareTestConstants.money;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.hamcrest.MatcherAssert;
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

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.enums.SplitFareStatus;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.FarePaymentDslRepository;
import com.rideaustin.rest.exception.ConflictException;
import com.rideaustin.rest.model.SplitFareDto;
import com.rideaustin.service.farepayment.FarePaymentService;
import com.rideaustin.service.promocodes.PromocodeUseResult;

@RunWith(MockitoJUnitRunner.class)
public class FarePaymentServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private FarePaymentDslRepository farePaymentDslRepository;
  @Mock
  private FareService fareService;

  private FarePaymentService farePaymentService;

  private Ride ride = new Ride();

  @Before
  public void setup() throws Exception {
    ride = prepareRide(RideStatus.COMPLETED);

    farePaymentService = new FarePaymentService(farePaymentDslRepository, fareService);
  }

  @Test
  public void testPrepareFareForMainRider() throws Exception {
    when(farePaymentDslRepository.save(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
    when(farePaymentDslRepository.findMainRiderFarePayment(anyLong())).thenReturn(Optional.empty());

    FarePayment fp = farePaymentService.createFarePaymentForMainRider(ride);

    assertThat(fp.getRide().getId(), is(ride.getId()));
    assertThat(fp.isMainRider(), is(true));
    assertThat(fp.getSplitStatus(), is(SplitFareStatus.ACCEPTED));
    assertThat(fp.getRider().getId(), is(ride.getRider().getId()));
  }

  @Test
  public void testPrepareFareForMainRiderAlreadyCreated() throws Exception {

    FarePayment mocked = new FarePayment();
    mocked.setRider(ride.getRider());
    mocked.setMainRider(true);
    mocked.setRide(ride);
    mocked.setId(123);

    when(farePaymentDslRepository.findFarePayment(any(), any())).thenReturn(mocked);
    FarePayment fp = farePaymentService.createFarePaymentForMainRider(ride);
    assertThat(fp.getRide().getId(), is(ride.getId()));
    assertThat(fp.isMainRider(), is(true));
    assertThat(fp.getId(), is(123L));
  }

  @Test
  public void testPrepareFareForMainRiderAlreadyWrongCreated() throws Exception {

    FarePayment mocked = new FarePayment();
    mocked.setRider(ride.getRider());
    mocked.setMainRider(false);
    mocked.setRide(ride);
    mocked.setId(123);
    expectedException.expect(ConflictException.class);
    expectedException.expectMessage("This ride has already created fare payment for ride main ride, but not as main rider");
    when(farePaymentDslRepository.findFarePayment(any(), any())).thenReturn(mocked);
    farePaymentService.createFarePaymentForMainRider(ride);
  }

  @Test
  public void testPrepareFareForMainRiderAlreadyWrongCreated_1() throws ConflictException {
    FarePayment mocked = new FarePayment();
    mocked.setRider(ride.getRider());
    mocked.setMainRider(false);
    mocked.setRide(ride);
    mocked.setId(123);
    expectedException.expect(ConflictException.class);
    expectedException.expectMessage("This ride has already created fare payment for other rider as main rider ");
    when(farePaymentDslRepository.findMainRiderFarePayment(anyLong())).thenReturn(Optional.of(mocked));
    farePaymentService.createFarePaymentForMainRider(ride);
  }

  @Test
  public void testUpdateFarePayment() {
    FarePayment farePayment = new FarePayment();
    farePaymentService.updateFarePayment(farePayment);

    verify(farePaymentDslRepository, times(1)).save(farePayment);
  }

  @Test
  public void testGetAcceptedPaymentParticipants() {
    when(farePaymentDslRepository.findAcceptedFarePayments(anyLong())).thenReturn(ImmutableList.of(
      mockFarePayment(ride, SplitFareStatus.ACCEPTED, false)
    ));

    List<FarePayment> result = farePaymentService.getAcceptedPaymentParticipants(ride.getId());

    assertEquals(1, result.size());
    assertEquals(SplitFareStatus.ACCEPTED, result.get(0).getSplitStatus());
  }

  @Test
  public void testCreateFarePaymentInfoBuildsInfoForRiderCancelledRide() throws ConflictException {
    testCreateFarePaymentInfoForCancelledRide(RideStatus.RIDER_CANCELLED);
  }

  @Test
  public void testCreateFarePaymentInfoBuildsInfoForDriverCancelledRide() throws ConflictException {
    testCreateFarePaymentInfoForCancelledRide(RideStatus.DRIVER_CANCELLED);
  }

  @Test
  public void testCreateFarePaymentInfoThrowsExceptionForCompletedRideIfNoPaymentsWereAccepted() throws ConflictException {
    when(farePaymentDslRepository.findFarePayments(anyLong())).thenReturn(ImmutableList.of(
      mockFarePayment(ride, SplitFareStatus.DECLINED),
      mockFarePayment(ride, SplitFareStatus.REQUESTED)
    ));
    expectedException.expect(ConflictException.class);
    expectedException.expectMessage("No fare payment recorded");

    farePaymentService.createFarePaymentInfo(ride, null, false);
  }

  @Test
  public void testCreateFarePaymentInfoThrowsExceptionForCompletedRideIfRidersAreDifferent() throws ConflictException {
    FarePayment acceptedPayment = mockFarePayment(ride, SplitFareStatus.ACCEPTED, true);
    Rider rider = new Rider();
    rider.setId(555L);
    acceptedPayment.setRider(rider);
    when(farePaymentDslRepository.findAcceptedFarePayments(anyLong())).thenReturn(ImmutableList.of(
      acceptedPayment
    ));
    final Optional<FareDetails> fareDetails = Optional.of(FareDetails.builder().totalFare(money(10)).build());
    when(fareService.calculateFinalFare(ride, null, false)).thenReturn(fareDetails);
    when(fareService.calculateTotalFare(ride, null, false, false)).thenReturn(fareDetails);
    when(fareService.checkCampaignCoverage(any(), any())).thenReturn(new FareService.CampaignCoverageResult(null, false));

    expectedException.expect(ConflictException.class);
    expectedException.expectMessage("Main rider on fare payment is different that main rider of ride");

    farePaymentService.createFarePaymentInfo(ride, null, false);
  }

  @Test
  public void testCreateFarePaymentInfo() throws ConflictException {
    FarePayment acceptedPrimaryPayment = mockFarePayment(ride, SplitFareStatus.ACCEPTED, true);
    FarePayment acceptedSecondaryPayment = mockFarePayment(ride, SplitFareStatus.ACCEPTED, false);
    when(farePaymentDslRepository.findAcceptedFarePayments(anyLong())).thenReturn(ImmutableList.of(
      acceptedPrimaryPayment,
      acceptedSecondaryPayment
    ));
    final Optional<FareDetails> fareDetails = Optional.of(FareDetails.builder().totalFare(money(10)).build());
    when(fareService.calculateTotalFare(ride, null, false, false)).thenReturn(fareDetails);
    when(fareService.calculateFinalFare(ride, null, false)).thenReturn(fareDetails);
    when(fareService.checkCampaignCoverage(any(), any())).thenReturn(new FareService.CampaignCoverageResult(null, false));

    FarePaymentService.FarePaymentInfo info = farePaymentService.createFarePaymentInfo(ride, null, false);

    assertNotNull(info);
    assertEquals(acceptedPrimaryPayment.getId(), info.getPrimaryRiderFarePayment().getId());
    assertEquals(1, info.getSecondaryRiderPayments().size());
    assertThat(acceptedSecondaryPayment.getId(), isIn(info.getSecondaryRiderPayments().stream().map(FarePayment::getId).collect(Collectors.toList())));
    MatcherAssert.assertThat(money(5).getAmount(), is(closeTo(info.getFarePerParticipants().getAmount(), BigDecimal.ZERO)));
    MatcherAssert.assertThat(money(5).getAmount(), is(closeTo(info.getPrimaryRiderFare().getAmount(), BigDecimal.ZERO)));
  }

  @Test
  public void testCreateFarePaymentInfoNotDividesTipBetweenRiders() throws Exception {
    FarePayment acceptedPrimaryPayment = mockFarePayment(ride, SplitFareStatus.ACCEPTED, true);
    FarePayment acceptedSecondaryPayment = mockFarePayment(ride, SplitFareStatus.ACCEPTED, false);
    when(farePaymentDslRepository.findAcceptedFarePayments(anyLong())).thenReturn(ImmutableList.of(
      acceptedPrimaryPayment,
      acceptedSecondaryPayment
    ));
    final Optional<FareDetails> fareDetails = Optional.of(FareDetails.builder()
      .totalFare(money(10))
      .tip(money(2))
      .build());
    when(fareService.calculateTotalFare(ride, null, false, false)).thenReturn(fareDetails);
    when(fareService.calculateFinalFare(ride, null, false)).thenReturn(fareDetails);
    when(fareService.checkCampaignCoverage(any(), any())).thenReturn(new FareService.CampaignCoverageResult(null, false));

    FarePaymentService.FarePaymentInfo info = farePaymentService.createFarePaymentInfo(ride, null, false);

    assertNotNull(info);
    assertThat(info.getFarePerParticipants().getAmount().doubleValue(), is(closeTo(5, 0.0)));
    assertThat(info.getPrimaryRiderFare().getAmount().doubleValue(), is(closeTo(7, 0.0)));
  }

  private void testCreateFarePaymentInfoForCancelledRide(RideStatus status) throws ConflictException {
    ride = prepareRide(status);
    FarePayment farePayment = mockFarePayment(ride, SplitFareStatus.ACCEPTED, false);
    when(farePaymentDslRepository.findFarePayment(anyLong(), anyLong())).thenReturn(farePayment);
    Money cancellationFee = Money.of(CurrencyUnit.USD, 5.0);
    when(fareService.calculateFinalFare(eq(ride), any(PromocodeUseResult.class), anyBoolean())).thenReturn(Optional.of(FareDetails.builder().cancellationFee(cancellationFee).build()));

    FarePaymentService.FarePaymentInfo info = farePaymentService.createFarePaymentInfo(ride, null, false);

    assertNotNull(info);
    assertEquals(farePayment, info.getPrimaryRiderFarePayment());
    assertNotNull(info.getSecondaryRiderPayments());
    assertTrue(info.getSecondaryRiderPayments().isEmpty());
    assertThat(info.getPrimaryRiderFare().getAmount(), is(closeTo(cancellationFee.getAmount(), BigDecimal.ZERO)));
    assertThat(info.getFarePerParticipants().getAmount(), is(closeTo(cancellationFee.getAmount(), BigDecimal.ZERO)));
  }

  private Ride prepareRide(RideStatus status) {
    Ride ride = new Ride();
    ride.setId(ThreadLocalRandom.current().nextInt());
    ride.setRequestedCarType(mockCarType());
    ride.setRider(new Rider());
    ride.setStatus(status);
    ride.setDistanceTravelled(new BigDecimal(1000));
    ride.setStartedOn(new DateTime().minusHours(5).toDate());
    ride.setCompletedOn(new Date());
    FareDetails fareDetails = FareDetails.builder()
      .distanceFare(money(1d))
      .totalFare(money(10d))
      .build();
    ride.setFareDetails(fareDetails);
    return ride;
  }

  private CarType mockCarType() {
    return new CarType();
  }

  private FarePayment mockFarePayment(Ride ride, SplitFareStatus status, boolean isPrimary) {
    FarePayment mocked = new FarePayment();
    mocked.setRider(ride.getRider());
    mocked.setMainRider(isPrimary);
    mocked.setRide(ride);
    mocked.setSplitStatus(status);
    mocked.setId(ThreadLocalRandom.current().nextInt());
    return mocked;
  }

  private SplitFareDto mockFarePayment(Ride ride, SplitFareStatus status) {
    return SplitFareDto.builder()
      .riderId(ride.getRider().getId())
      .rideId(ride.getId())
      .status(status)
      .id(ThreadLocalRandom.current().nextLong())
      .build();
  }

}