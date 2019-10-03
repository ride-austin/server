package com.rideaustin.service.email;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.user.User;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.utils.RandomUtils;
import com.rideaustin.testrail.TestCases;

/**
 * Covers C1176985 and C1176994
 */
@Category(Email.class)
public class RiderPasswordResetEmail_C1176985IT extends AbstractEmailTest {

  public void shouldSendPasswordResetEmail_WhenUserIsFound() throws Exception {
    final User user = newRegisteredUser();

    userAction.forgotPassword(user.getEmail())
      .andExpect(status().isOk());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).passwordResetDelivered(startDate, user.getEmail());
  }

  @Test
  @TestCases("C1176985")
  public void shouldNotSendPasswordResetEmail_WhenUserIsNotFound() throws Exception {
    final String email = RandomUtils.randomEmail();

    userAction.forgotPassword(email)
      .andExpect(status().isBadRequest());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).passwordResetNotDelivered(startDate, email);
  }
}
