package com.rideaustin.service.email;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.user.Rider;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.testrail.TestCases;

@Category(Email.class)
public class RiderActivatedDeactivedEmail_C1176986IT extends AbstractEmailTest {

  @Test
  @TestCases("C1176986")
  public void shouldSendActivationEmail_WhenAdminUpdates() throws Exception {
    administrator = administratorFixture.getFixture();
    Rider rider = riderFixtureProvider.create().getFixture();
    rider.setActive(false);
    Rider activatedRider = activatedRider(rider);

    administratorAction.updateRider(administrator.getEmail(), rider.getId(), activatedRider)
      .andExpect(status().isOk());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).userActivatedEmailDelivered(startDate, rider.getEmail());
  }

  @Test
  @TestCases("C1176986")
  public void shouldNotSendActivationEmail_WhenRiderUpdates() throws Exception {
    Rider rider = riderFixtureProvider.create().getFixture();
    rider.setActive(false);
    Rider activatedRider = activatedRider(rider);

    riderAction.updateRider(rider.getEmail(), rider.getId(), activatedRider)
      .andExpect(status().isOk());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).userActivatedEmailNotDelivered(startDate, rider.getEmail());
  }

  @Test
  @TestCases("C1176986")
  public void shouldSendDeactivationEmail_WhenAdminUpdates() throws Exception {
    administrator = administratorFixture.getFixture();
    Rider rider = riderFixtureProvider.create().getFixture();
    Rider deactivatedRider = deactivatedRider(rider);

    administratorAction.updateRider(administrator.getEmail(), rider.getId(), deactivatedRider)
      .andExpect(status().isOk());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).userDeactivatedEmailDelivered(startDate, rider.getEmail());
  }

  @Test
  @TestCases("C1176986")
  public void shouldNotSendDeactivationEmail_WhenRiderUpdates() throws Exception {
    Rider rider = riderFixtureProvider.create().getFixture();
    Rider deactivatedRider = deactivatedRider(rider);

    riderAction.updateRider(rider.getEmail(), rider.getId(), deactivatedRider)
      .andExpect(status().isOk());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).userDeactivatedEmailNotDelivered(startDate, rider.getEmail());
  }

  private Rider deactivatedRider(Rider rider) {
    return update(rider, Rider::new, target -> target.setActive(false));
  }

  private Rider activatedRider(Rider rider) {
    return update(rider, Rider::new, target -> target.setActive(true));
  }

}
