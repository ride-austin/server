package com.rideaustin.service.email;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.RideAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.SetupAction;
import com.rideaustin.test.stubs.transactional.PaymentService;

@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public abstract class AbstractTripSummaryEmailNonTxTest<T extends SetupAction<T>> extends AbstractNonTxTests<T> {

  @Inject
  protected RideAction rideAction;
  @Inject
  protected EmailCheckerService emailCheckerService;
  @Inject
  protected PaymentService paymentService;
  protected Date startDate;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = setup.setUp();
    startDate = Date.from(Instant.now().minus(10, ChronoUnit.SECONDS));
  }

  protected List<InterceptingEmailService.Email> fetchEmailsWithSleep() {
    sleeper.sleep(2000);
    return emailCheckerService.fetchEmails(5);
  }
}
