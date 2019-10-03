package com.rideaustin.payment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.repo.dsl.RiderCardDslRepository;
import com.rideaustin.service.thirdparty.StripeServiceMockImpl;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.RideAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.RA13663Setup;
import com.rideaustin.test.stubs.transactional.PaymentService;

@Category(Payment.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class RA13663TwoChargesOneDayIT extends AbstractNonTxTests<RA13663Setup> {

  @Inject
  private RideAction rideAction;
  @Inject
  private StripeServiceMockImpl stripeServiceMock;
  @Inject
  private PaymentService paymentService;
  @Inject
  private RiderCardDslRepository cardDslRepository;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  public void test() throws Exception {
    Rider rider = setup.getRider();

    Long ride = rideAction.performRide(rider, locationProvider.getCenter(), locationProvider.getOutsideAirportLocation(), setup.getActiveDriver());

    stripeServiceMock.setFailOnCardCharge(true);

    paymentService.processRidePayment(ride);

    List<RiderCard> cards = cardDslRepository.findByRider(rider);

    assertEquals(1, cards.size());
    assertEquals(1, cards.get(0).getFailedChargeAttempts());
    assertNotNull(cards.get(0).getLastFailureDate());

    paymentService.processRidePayment(ride);

    Ride one = rideDslRepository.findOne(ride);
    assertEquals(PaymentStatus.UNPAID, one.getPaymentStatus());
  }
}
