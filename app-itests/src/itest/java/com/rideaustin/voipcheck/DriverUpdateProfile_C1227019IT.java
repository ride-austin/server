package com.rideaustin.voipcheck;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.user.Driver;
import com.rideaustin.testrail.TestCases;

@Category(VoipCheck.class)
public class DriverUpdateProfile_C1227019IT extends AbstractVoipCheckTest {
  @Test
  @TestCases("C1227019")
  public void shouldNotAccept_DuringPhoneVerification_WithVoipNumber() throws Exception {
    Driver driver = driverFixtureProvider.create().getFixture();

    driverAction.requestPhoneVerificationCode(driver.getEmail(), voipPhoneNumber)
      .andExpect(status().isBadRequest())
      .andExpect(content().string("\"SMS verification failed\""));
  }
}
