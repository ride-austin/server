package com.rideaustin.service.email;

import static com.rideaustin.Constants.City.AUSTIN;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.user.User;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.testrail.TestCases;

/**
 * Covers C1176983 and C1176995
 */
@Category(Email.class)
public class RiderSignUpEmail_C1176983IT extends AbstractEmailTest {

  @Test
  @TestCases("C1176983")
  public void shouldSendSignUpEmail_WhenSignUpIsSuccessful() throws Exception {
    User user = newUnregisteredUser();

    userAction.signUp(user.getEmail(), user.getFacebookId(), user.getFirstname(), user.getLastname(), user.getPassword(),
      user.getPhoneNumber(), null, true, AUSTIN.getId())
      .andExpect(status().isOk());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).riderSignUpEmailDelivered(startDate, user.getEmail());
  }

  @Test
  @TestCases("C1176995")
  public void shouldNotSendSignUpEmail_WhenSignUpIsUnsuccessful() throws Exception {
    final String invalidFirstName = StringUtils.EMPTY;
    User user = newUnregisteredUser();

    userAction.signUp(user.getEmail(), user.getFacebookId(), invalidFirstName, user.getLastname(), user.getPassword(),
      user.getPhoneNumber(), null, true, AUSTIN.getId())
      .andExpect(status().isUnprocessableEntity());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).riderSignUpEmailNotDelivered(startDate, user.getEmail());
  }
}
