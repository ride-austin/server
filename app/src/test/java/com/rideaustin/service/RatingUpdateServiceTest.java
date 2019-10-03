package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static com.rideaustin.test.util.TestUtils.RANDOM;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.RatingUpdate;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.repo.dsl.RatingUpdateDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.service.rating.DriverRatingService;
import com.rideaustin.service.rating.RatingUpdateService;
import com.rideaustin.service.rating.RiderRatingService;

@RunWith(MockitoJUnitRunner.class)
public class RatingUpdateServiceTest {

  private static final String COMMENT = "test comment";

  @Mock
  private RatingUpdateDslRepository ratingUpdateDslRepository;
  @Mock
  private DriverRatingService driverRatingService;
  @Mock
  private RiderRatingService riderRatingService;
  @Mock
  private RiderDslRepository riderDslRepository;
  @Mock
  private DriverDslRepository driverDslRepository;

  private RatingUpdateService ratingUpdateService;

  private Ride ride = new Ride();

  @Before
  public void setup() throws Exception {
    ride = prepareRide();

    ratingUpdateService = new RatingUpdateService(ratingUpdateDslRepository, driverRatingService, riderRatingService,
      riderDslRepository, driverDslRepository);
  }

  @Test
  public void testSaveNewRatingUpdate() {
    Rider rider = new Rider();
    rider.setId(RANDOM.nextInt());
    User riderUser = new User();
    riderUser.setFirstname("John");
    riderUser.setLastname("smith");
    rider.setUser(riderUser);

    Driver driver = new Driver();
    driver.setId(RANDOM.nextInt());
    driver.setUser(riderUser);
    RatingUpdate ratingUpdate = ratingUpdateService.saveNewRatingUpdate(driver, rider, 3d, ride, COMMENT);

    assertThat(ratingUpdate.getRating(), is(3d));
    assertThat(ratingUpdate.getComment(), is(COMMENT));
  }

  private Ride prepareRide() {
    Ride ride = new Ride();
    ride.setId(RANDOM.nextInt());
    ride.setRider(new Rider());
    ride.setDistanceTravelled(new BigDecimal(1000));
    ride.setStartedOn(new DateTime().minusHours(5).toDate());
    ride.setCompletedOn(new Date());
    FareDetails fareDetails = FareDetails.builder()
      .distanceFare(Money.of(CurrencyUnit.USD, 1d))
      .totalFare(Money.of(CurrencyUnit.USD, 10d))
      .build();
    ride.setFareDetails(fareDetails);
    return ride;
  }

}
