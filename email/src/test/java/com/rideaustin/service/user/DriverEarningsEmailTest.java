package com.rideaustin.service.user;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;

import org.apache.commons.mail.EmailException;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.City;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.service.model.DriverEarnings;

public class DriverEarningsEmailTest {

  @Test
  public void testRecipientIsResolvedFromEarnings() throws EmailException {
    final Driver driver = new Driver();
    final User user = new User();
    final String address = "abc@de.ee";
    user.setEmail(address);
    driver.setUser(user);
    final City city = new City();
    city.setContactEmail("contact@email.com");
    final DriverEarningsEmail email = new DriverEarningsEmail(
      new DriverEarnings(LocalDate.now(), driver, ZoneId.of("UTC")),
      null, city);

    assertEquals(1, email.getRecipientList().size());
    assertEquals(address, email.getRecipientList().get(0));
  }

  @Test
  public void testRecipientIsResolvedFromArguments() throws EmailException {
    final String address = "abc@de.ee";
    final City city = new City();
    city.setContactEmail("contact@email.com");
    final Driver driver = new Driver();
    driver.setUser(new User());
    final DriverEarningsEmail email = new DriverEarningsEmail(
      new DriverEarnings(LocalDate.now(), driver, ZoneId.of("UTC")),
      ImmutableList.of(address), city);

    assertEquals(1, email.getRecipientList().size());
    assertEquals(address, email.getRecipientList().get(0));
  }

}