package com.rideaustin.voipcheck;

import static com.rideaustin.Constants.City.AUSTIN;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.Constants;
import com.rideaustin.model.user.User;
import com.rideaustin.testrail.TestCases;

/**
 * Covers C1227010 and C1227011
 */
@Category(VoipCheck.class)
public class RiderRegistration_C1227010IT extends AbstractVoipCheckTest {

  @Test
  @TestCases("C1227010")
  public void shouldReject_DuringUserValidation() throws Exception {
    User user = newUnregisteredUser(voipPhoneNumber);

    userAction.userExists(user.getEmail(), user.getPhoneNumber())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(String.format("\"%s\"", Constants.ErrorMessages.PHONE_NUMBER_NO_VOIP)));
  }

  @Test
  @TestCases("C1227010")
  public void shouldAccept_DuringUserValidation() throws Exception {
    User user = newUnregisteredUser(validPhoneNumber);

    userAction.userExists(user.getEmail(), user.getPhoneNumber())
      .andExpect(status().isOk());
  }

  @Test
  @TestCases("C1227010")
  public void shouldReject_DuringPhoneVerification() throws Exception {
    User user = newUnregisteredUser(voipPhoneNumber);

    userAction.requestPhoneVerificationCode(user.getEmail(), user.getPhoneNumber())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(String.format("\"%s\"", Constants.ErrorMessages.PHONE_NUMBER_NO_VOIP)));
  }

  @Test
  @TestCases("C1227010")
  public void shouldReject_DuringUserCreation() throws Exception {
    User user = newUnregisteredUser(voipPhoneNumber);

    userAction.signUp(user, AUSTIN.getId())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(String.format("\"%s\"", Constants.ErrorMessages.PHONE_NUMBER_NO_VOIP)));
  }

  @Test
  @TestCases("C1227010")
  public void shouldAccept_DuringUserCreation() throws Exception {
    User user = newUnregisteredUser(validPhoneNumber);

    userAction.signUp(user, AUSTIN.getId())
      .andExpect(status().isOk());
  }
}
