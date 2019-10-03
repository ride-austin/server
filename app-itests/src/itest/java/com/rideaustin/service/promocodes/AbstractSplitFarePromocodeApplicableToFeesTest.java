package com.rideaustin.service.promocodes;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.farepayment.FarePaymentService;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.AbstractSplitFarePromocodeApplicableToFeesSetup;
import com.rideaustin.test.stubs.transactional.PaymentService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractSplitFarePromocodeApplicableToFeesTest<T extends AbstractSplitFarePromocodeApplicableToFeesSetup<T>> extends AbstractNonTxPromocodeTest<T> {

  protected Promocode promocode;
  protected Rider rider;
  protected ActiveDriver activeDriver;

  @Inject
  protected RiderAction riderAction;
  @Inject
  protected DriverAction driverAction;

  @Inject
  protected RideDispatchServiceConfig config;
  @Inject
  protected PaymentService paymentService;
  @Inject
  protected FarePaymentService farePaymentService;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    this.rider = setup.getRider();
    this.activeDriver = setup.getActiveDriver();
    this.promocode = setup.getPromocode();
  }
}
