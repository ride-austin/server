package com.rideaustin.service.ride;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.rideaustin.model.City;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.utils.DateUtils;

public abstract class BaseEmailTest {

  protected Ride setupRide(FareDetails fareDetails, ActiveDriver activeDriver) {
    final Ride ride = new Ride();
    ride.setFareDetails(fareDetails);
    ride.setCreatedDate(DateUtils.localDateTimeToDate(LocalDateTime.of(2019, 12, 31, 23, 59, 59), ZoneId.of("UTC")));
    ride.setStartedOn(new Date());
    ride.setCompletedOn(new Date());
    ride.setRequestedCarType(new CarType());
    ride.setActiveDriver(activeDriver);
    return ride;
  }

  protected FareDetails setupFareDetails() {
    final FareDetails fareDetails = new FareDetails();
    fareDetails.setNormalFare(money(10.0));
    fareDetails.setSubTotal(money(10.0));
    fareDetails.setMinimumFare(money(4.0));
    return fareDetails;
  }

  protected ActiveDriver setupActiveDriver() {
    final ActiveDriver activeDriver = new ActiveDriver();
    final Driver driver = new Driver();
    driver.setUser(new User());
    activeDriver.setDriver(driver);
    return activeDriver;
  }

  protected Rider setupRider() {
    final Rider rider = new Rider();
    final User riderUser = new User();
    riderUser.setFirstname("A");
    riderUser.setLastname("B");
    rider.setUser(riderUser);
    return rider;
  }

  protected City setupCity() {
    final City city = new City();
    city.setAppName("Test");
    city.setContactEmail("contact@test.com");
    return city;
  }

  protected static Money money(double amount) {
    return Money.of(CurrencyUnit.USD, BigDecimal.valueOf(amount));
  }
}
