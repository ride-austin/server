package com.rideaustin.service.promocodes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.rideaustin.service.email.InterceptingEmailService.Email;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.testrail.TestCases;

public class C1176991ApplicableIT extends AbstractApplicableToFeesPromocodeTest {

  @Inject
  private EmailCheckerService emailCheckerService;

  private Date startDate;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    startDate = Date.from(Instant.now().minus(1, ChronoUnit.SECONDS));
  }

  @Test
  @TestCases("C1176991")
  public void testApplicable() throws Exception {
    this.promocode = setup.getApplicablePromocode();
    doTest();
  }

  @Override
  protected void doAssert(Long ride) throws Exception {
    List<Email> messages = emailCheckerService.fetchEmails(5);
    EmailAssert.assertThat(messages)
      .tripSummaryEmailDeliveredWithRideCredit(startDate, rider.getEmail());
  }

  @After
  public void tearDown() throws Exception {
    super.supportTearDown();
    emailCheckerService.close();
  }
}
