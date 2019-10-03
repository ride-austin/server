package com.rideaustin.signup.driver;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.user.Driver;

@Category(DriverSignup.class)
public class RA10795DriverAgreementDateIT extends AbstractDriverSignupTest {

  @Test
  public void test() throws Exception {
    Date now = new Date();
    Driver driver = createDriver(rider);

    riderAction.addDriver(rider.getEmail(), 1L, driver)
      .andExpect(status().isOk());

    int drivers = countRowsInTable("drivers");
    assertEquals("Expected to have 1 driver", 1, drivers);
    Date agreementDate = jdbcTemplate.queryForObject("select agreement_date from drivers limit 1", Date.class);
    assertEquals("Expected to match years", LocalDate.fromDateFields(now).getYear(), LocalDate.fromDateFields(agreementDate).getYear());
    assertEquals("Expected to match months",LocalDate.fromDateFields(now).getMonthOfYear(), LocalDate.fromDateFields(agreementDate).getMonthOfYear());
    assertEquals("Expected to match days",LocalDate.fromDateFields(now).getDayOfMonth(), LocalDate.fromDateFields(agreementDate).getDayOfMonth());
  }
}
