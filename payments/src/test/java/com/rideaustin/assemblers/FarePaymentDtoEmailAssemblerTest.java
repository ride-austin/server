package com.rideaustin.assemblers;

import static com.rideaustin.service.FareTestConstants.money;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.RiderCardDto;
import com.rideaustin.service.model.FarePaymentDto;
import com.rideaustin.utils.RandomString;

public class FarePaymentDtoEmailAssemblerTest {

  @Mock
  private RiderCardDtoAssembler cardDtoAssembler;

  private FarePaymentDtoEmailAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new FarePaymentDtoEmailAssembler(cardDtoAssembler);
  }

  @Test
  public void toDtoSkipsNull() {
    final FarePaymentDto result = testedInstance.toDto((FarePayment) null);

    assertNull(result);
  }

  @Test
  public void toDtoFillsInfo() {
    FarePayment source = new FarePayment();
    final Rider rider = new Rider();
    final User user = new User();
    user.setFirstname("A");
    user.setLastname("B");
    rider.setUser(user);
    source.setRider(rider);
    source.setChargeId(RandomString.generate());
    source.setProvider(PaymentProvider.CREDIT_CARD);
    source.setPaymentStatus(PaymentStatus.PAID);
    source.setMainRider(true);
    source.setStripeCreditCharge(money(10.0));
    source.setFreeCreditCharged(money(10.0));
    final RiderCard usedCard = new RiderCard();
    source.setUsedCard(usedCard);

    when(cardDtoAssembler.toDto(usedCard)).thenReturn(RiderCardDto.builder().build());

    final FarePaymentDto result = testedInstance.toDto(source);

    assertEquals(source.getPaymentStatus(), result.getPaymentStatus());
    assertEquals(source.getRider().getFullName(), result.getRiderFullName());
    assertEquals(source.getStripeCreditCharge(), result.getStripeCreditCharge());
    assertEquals(source.getFreeCreditCharged(), result.getFreeCreditCharged());
    assertEquals(source.isMainRider(), result.isMainRider());
    assertEquals(source.getProvider(), result.getPaymentProvider());
    assertNotNull(result.getUsedCard());
  }
}