package com.rideaustin.assemblers;

import static com.rideaustin.service.FareTestConstants.money;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.Constants;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.SplitFareStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.FarePaymentDto;
import com.rideaustin.rest.model.RiderCardDto;
import com.rideaustin.utils.DateUtils;
import com.rideaustin.utils.RandomString;

public class FarePaymentDtoAssemblerTest {

  @Mock
  private RiderCardDtoAssembler cardDtoAssembler;

  private FarePaymentDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new FarePaymentDtoAssembler(cardDtoAssembler);
  }

  @Test
  public void toDtoSkipsNull() {
    final FarePaymentDto result = testedInstance.toDto((FarePayment) null);

    assertNull(result);
  }

  @Test
  public void toDtoFillsInfo() {
    FarePayment source = createFarePayment();

    final FarePaymentDto result = testedInstance.toDto(source);

    assertEquals(source.getId(), result.getId());
    assertEquals(source.getRider().getId(), result.getRiderId());
    assertEquals(source.getRider().getFullName(), result.getRiderFullName());
    assertEquals(source.getRider().getUser().getPhotoUrl(), result.getRiderPhoto());
    assertEquals(source.getRide().getId(), result.getRideId());
    assertEquals(source.getSplitStatus(), result.getStatus());
    assertEquals("12/31/2019", result.getCreatedDate());
    assertEquals(source.getChargeId(), result.getChargeId());
    assertEquals(source.getProvider(), result.getPaymentProvider());
    assertEquals(source.getPaymentStatus(), result.getPaymentStatus());
    assertEquals(source.isMainRider(), result.isMainRider());
  }

  @Test
  public void toDtoSetsUsedCard() {
    final FarePayment source = createFarePayment();
    source.setUsedCard(new RiderCard());
    when(cardDtoAssembler.toDto(any(RiderCard.class))).thenReturn(RiderCardDto.builder().build());

    final FarePaymentDto result = testedInstance.toDto(source);

    assertNotNull(result.getUsedCard());
  }

  @Test
  public void toDtoSetsUpdatedDate() {
    final FarePayment source = createFarePayment();
    source.setUpdatedDate(DateUtils.localDateToDate(LocalDate.of(2019, 12, 31), Constants.CST_ZONE));

    final FarePaymentDto result = testedInstance.toDto(source);

    assertEquals("12/31/2019", result.getUpdatedDate());
  }

  @Test
  public void toDtoSetsFreeCreditCharged() {
    final FarePayment source = createFarePayment();
    source.setFreeCreditCharged(money(10));

    final FarePaymentDto result = testedInstance.toDto(source);

    assertEquals(money(10.0), result.getFreeCreditCharged());
  }

  @Test
  public void toDtoSetsStripeCreditCharged() {
    final FarePayment source = createFarePayment();
    source.setStripeCreditCharge(money(10));

    final FarePaymentDto result = testedInstance.toDto(source);

    assertEquals(money(10.0), result.getStripeCreditCharge());
  }

  private FarePayment createFarePayment() {
    FarePayment source = new FarePayment();
    source.setId(1L);
    final Rider rider = new Rider();
    final User user = new User();
    final Ride ride = new Ride();
    ride.setId(1L);
    user.setFirstname("A");
    user.setLastname("B");
    user.setPhotoUrl("url");
    rider.setUser(user);
    source.setRider(rider);
    source.setRide(ride);
    source.setSplitStatus(SplitFareStatus.ACCEPTED);
    source.setCreatedDate(DateUtils.localDateToDate(LocalDate.of(2019, 12, 31), Constants.CST_ZONE));
    source.setChargeId(RandomString.generate());
    source.setProvider(PaymentProvider.CREDIT_CARD);
    source.setPaymentStatus(PaymentStatus.PAID);
    source.setMainRider(true);
    return source;
  }
}