package com.rideaustin.voipcheck;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.Constants;
import com.rideaustin.model.user.Rider;
import com.rideaustin.testrail.TestCases;

@Category(VoipCheck.class)
public class RiderUpdateProfile_C1227029IT extends AbstractVoipCheckTest {

  @Test
  @TestCases("C1227029")
  public void shouldAccept_WhenVoipNumberIsRemoved() throws Exception {
    administrator = administratorFixture.getFixture();
    Rider rider = riderFixtureProvider.create().getFixture();
    Rider updatedRider = updatedRider(rider, voipPhoneNumber);

    administratorAction.updateRider(administrator.getEmail(), rider.getId(), updatedRider)
      .andExpect(status().isBadRequest())
      .andExpect(content().string(String.format("\"%s\"", Constants.ErrorMessages.PHONE_NUMBER_NO_VOIP)));

    updatedRider = updatedRider(rider, validPhoneNumber);

    administratorAction.updateRider(administrator.getEmail(), rider.getId(), updatedRider)
      .andExpect(status().isOk());
  }
}
