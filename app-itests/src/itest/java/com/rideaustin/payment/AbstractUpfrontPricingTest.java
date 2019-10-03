package com.rideaustin.payment;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jayway.awaitility.Awaitility;
import com.rideaustin.Constants;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.RideAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.SetupAction;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.test.stubs.transactional.PaymentService;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
public abstract class AbstractUpfrontPricingTest<T extends SetupAction<T>> extends AbstractNonTxTests<T> {

  @Inject
  protected PaymentService paymentService;
  @Inject
  protected ConfigurationItemCache configurationItemCache;
  @Inject
  protected RideDslRepository rideDslRepository;

  @Inject
  protected RideAction rideAction;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "upfrontEnabled", true);
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "upfrontTimeout", 1);
  }

  protected void assertUpfrontCharge(Long rideId) {
    Awaitility.await()
      .atMost(5, TimeUnit.SECONDS)
      .until(() -> {
        final Ride ride = rideDslRepository.findOne(rideId);
        final boolean charged = ride.getChargeId() != null;
        final boolean prepaid = ride.getPaymentStatus() == PaymentStatus.PREPAID_UPFRONT;
        return charged && prepaid;
      });
  }
}
