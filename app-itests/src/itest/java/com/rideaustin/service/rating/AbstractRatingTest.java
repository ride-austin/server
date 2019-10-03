package com.rideaustin.service.rating;

import java.math.BigDecimal;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.RandomUtils;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.RideFixture;
import com.rideaustin.test.fixtures.providers.RideFixtureProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractRatingTest extends ITestProfileSupport {

  @Inject
  @Named("rideFixtureProvider")
  protected RideFixtureProvider rideFixtureProvider;

  @Inject
  protected DriverAction driverAction;

  @Inject
  protected RiderAction riderAction;

  @Inject
  protected DriverDslRepository driverDslRepository;

  @Inject
  protected AbstractRatingService<Driver> driverRatingService;

  @Inject
  protected AbstractRatingService<Rider> riderRatingService;

  protected Ride newRide(RideStatus status) {
    RideFixture rideFixture = rideFixtureProvider.create(status, false);
    return rideFixture.getFixture();
  }

  protected Ride newRide(RideStatus status, boolean sameDriver) {
    RideFixture rideFixture = rideFixtureProvider.create(status, sameDriver);
    return rideFixture.getFixture();
  }

  protected BigDecimal[] randomRatings(int count) {
    BigDecimal[] ratings = new BigDecimal[count];
    for (int i = 0; i < count; i++) {
      ratings[i] = new BigDecimal(RandomUtils.nextInt(1, 6));
    }

    return ratings;
  }

  protected double average(BigDecimal[] ratings) {
    return Arrays.stream(ratings).mapToDouble(BigDecimal::doubleValue).average().getAsDouble();
  }
}
