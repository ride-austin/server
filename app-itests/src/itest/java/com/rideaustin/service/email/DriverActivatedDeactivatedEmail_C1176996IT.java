package com.rideaustin.service.email;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.ConsoleDriverDto;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.stubs.DriverDslRepository;
import com.rideaustin.testrail.TestCases;

@Category(Email.class)
public class DriverActivatedDeactivatedEmail_C1176996IT extends AbstractEmailTest {

  @Inject
  private DriverDslRepository driverDslRepository;

  @Test
  @TestCases("C1176996")
  public void shouldSendActivationEmail_WhenAdminUpdates() throws Exception {
    administrator = administratorFixture.getFixture();
    Driver driver = deactivatedDriver(driverFixtureProvider.create().getFixture());
    final ConsoleDriverDto consoleDriverDto = new ConsoleDriverDto(driver.getId(), driver.getCityApprovalStatus(), driver.getPayoneerStatus(), driver.getDirectConnectId(),
      driver.getSsn(), driver.getLicenseNumber(), driver.getLicenseState(),
      driver.getActivationNotes(), DriverActivationStatus.ACTIVE, driver.getFirstname(), driver.getUser().getMiddleName(),
      driver.getLastname(), driver.getUser().getNickName(), driver.getPhoneNumber(), driver.getUser().getDateOfBirth(),
      driver.getUser().getAddress(), driver.getEmail(), driver.getUser().getGender(), driver.getSpecialFlags(),
      driver.getGrantedDriverTypesBitmask(), driver.getRating());

    administratorAction.updateDriver(administrator.getEmail(), driver.getId(), consoleDriverDto)
      .andExpect(status().isOk());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).userActivatedEmailDelivered(startDate, driver.getEmail());
  }

  @Test
  @TestCases("C1176996")
  public void shouldSendDeactivationEmail_WhenAdminUpdates() throws Exception {
    administrator = administratorFixture.getFixture();
    Driver driver = driverFixtureProvider.create().getFixture();
    final ConsoleDriverDto consoleDriverDto = new ConsoleDriverDto(driver.getId(), driver.getCityApprovalStatus(), driver.getPayoneerStatus(), driver.getDirectConnectId(),
      driver.getSsn(), driver.getLicenseNumber(), driver.getLicenseState(),
      driver.getActivationNotes(), DriverActivationStatus.INACTIVE, driver.getFirstname(), driver.getUser().getMiddleName(),
      driver.getLastname(), driver.getUser().getNickName(), driver.getPhoneNumber(), driver.getUser().getDateOfBirth(),
      driver.getUser().getAddress(), driver.getEmail(), driver.getUser().getGender(), driver.getSpecialFlags(),
      driver.getGrantedDriverTypesBitmask(), driver.getRating());

    administratorAction.updateDriver(administrator.getEmail(), driver.getId(), consoleDriverDto)
      .andExpect(status().isOk());

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).userDeactivatedEmailDelivered(startDate, driver.getEmail());
  }

  private Driver deactivatedDriver(Driver driver) {
    return driverDslRepository.save(update(driver, Driver::new, target -> {
      target.setActive(false);
      target.setActivationStatus(DriverActivationStatus.INACTIVE);
    }));
  }

}
