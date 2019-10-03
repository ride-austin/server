package com.rideaustin.signup.driver;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.user.Driver;
import com.rideaustin.service.email.Email;
import com.rideaustin.service.email.InterceptingEmailService;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.testrail.TestCases;

@Category({Email.class, DriverSignup.class})
public class DriverSignUpEmail_C1176999IT extends AbstractDriverSignupTest {

  @Inject
  private EmailCheckerService emailCheckerService;
  private Date startDate;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    startDate = Date.from(Instant.now().minus(1, ChronoUnit.SECONDS));
  }

  @Test
  @TestCases("C1176999")
  public void shouldSendSignUpEmail_WhenSignUpIsSuccessful() throws Exception {
    Driver driver = createDriver(rider);

    riderAction.addDriver(rider.getEmail(), 1L, driver)
      .andExpect(status().isOk());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).driverSignUpEmailDelivered(startDate, rider.getEmail());
  }

  @Test
  @TestCases("C1176999")
  public void shouldNotSendSignUpEmail_WhenSignUpIsUnsuccessful() throws Exception {
    Driver driver = createDriver(rider);
    driver.setSsn(StringUtils.EMPTY);

    riderAction.addDriver(rider.getEmail(), 1L, driver)
      .andExpect(status().isBadRequest());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).driverSignUpEmailNotDelivered(startDate, rider.getEmail());
  }

  protected List<InterceptingEmailService.Email> fetchEmailsWithSleep() {
    sleeper.sleep(2000L);
    return emailCheckerService.fetchEmails(5);
  }

}
