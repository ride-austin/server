package com.rideaustin.voipcheck;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.ConsoleDriverDto;
import com.rideaustin.testrail.TestCases;

@Category(VoipCheck.class)
public class DriverUpdateProfile_C1230935IT extends AbstractVoipCheckTest {

  @Before
  public void setUp() throws Exception {
    super.setUp();
    administrator = administratorFixture.getFixture();
  }

  @Test
  @TestCases("C1230935")
  public void shouldAccept_WithVoipPhoneNumber() throws Exception {
    Driver driver = driverFixtureProvider.create().getFixture();
    final ConsoleDriverDto consoleDriverDto = new ConsoleDriverDto(driver.getId(), driver.getCityApprovalStatus(), driver.getPayoneerStatus(), driver.getDirectConnectId(),
      driver.getSsn(), driver.getLicenseNumber(), driver.getLicenseState(),
      driver.getActivationNotes(), DriverActivationStatus.ACTIVE, driver.getFirstname(), driver.getUser().getMiddleName(),
      driver.getLastname(), driver.getUser().getNickName(), voipPhoneNumber, driver.getUser().getDateOfBirth(),
      driver.getUser().getAddress(), driver.getEmail(), driver.getUser().getGender(), driver.getSpecialFlags(),
      driver.getGrantedDriverTypesBitmask(), driver.getRating());

    administratorAction.updateDriver(administrator.getEmail(), driver.getId(), consoleDriverDto)
      .andExpect(status().isOk());
  }
}
