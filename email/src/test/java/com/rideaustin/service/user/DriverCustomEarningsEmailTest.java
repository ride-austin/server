package com.rideaustin.service.user;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import org.apache.commons.mail.EmailException;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.service.model.DriverCustomEarnings;

public class DriverCustomEarningsEmailTest {

  @Test
  public void testRecipientIsResolvedFromEarnings() throws EmailException {
    final Driver driver = new Driver();
    final User user = new User();
    final String address = "abc@de.ee";
    user.setEmail(address);
    driver.setUser(user);
    final City city = new City();
    city.setContactEmail("contact@email.com");
    final DriverCustomEarningsEmail email = new DriverCustomEarningsEmail(
      new DriverCustomEarnings(LocalDate.now(), ZoneId.of("UTC"), driver, Constants.ZERO_USD, new ArrayList<>()),
      null, LocalDate.now(), city);

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
    final DriverCustomEarningsEmail email = new DriverCustomEarningsEmail(
      new DriverCustomEarnings(LocalDate.now(), ZoneId.of("UTC"), driver, Constants.ZERO_USD, new ArrayList<>()),
      ImmutableList.of(address), LocalDate.now(), city);

    assertEquals(1, email.getRecipientList().size());
    assertEquals(address, email.getRecipientList().get(0));
  }
}