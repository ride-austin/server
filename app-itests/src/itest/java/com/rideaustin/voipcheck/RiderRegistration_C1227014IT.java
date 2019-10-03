package com.rideaustin.voipcheck;

import static com.rideaustin.Constants.City.AUSTIN;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.Constants;
import com.rideaustin.model.user.User;
import com.rideaustin.testrail.TestCases;

@Category(VoipCheck.class)
public class RiderRegistration_C1227014IT extends AbstractVoipCheckTest {

  @Test
  @TestCases("C1227014")
  public void shouldAccept_WhenVoipNumberIsRemoved_DuringUserValidation() throws Exception {
    User user = newUnregisteredUser(voipPhoneNumber);

    userAction.userExists(user.getEmail(), user.getPhoneNumber())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(String.format("\"%s\"", Constants.ErrorMessages.PHONE_NUMBER_NO_VOIP)));

    user.setPhoneNumber(validPhoneNumber);

    userAction.userExists(user.getEmail(), user.getPhoneNumber())
      .andExpect(status().isOk());
  }

  @Test
  @TestCases("C1227014")
  public void shouldAccept_WhenVoipNumberIsRemoved_DuringUserCreation() throws Exception {
    User user = newUnregisteredUser(voipPhoneNumber);

    userAction.signUp(user, AUSTIN.getId())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(String.format("\"%s\"", Constants.ErrorMessages.PHONE_NUMBER_NO_VOIP)));

    user.setPhoneNumber(validPhoneNumber);

    userAction.signUp(user, AUSTIN.getId())
      .andExpect(status().isOk());
  }
}
