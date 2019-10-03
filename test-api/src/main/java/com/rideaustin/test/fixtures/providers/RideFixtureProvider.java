package com.rideaustin.test.fixtures.providers;

import static com.rideaustin.Constants.City.AUSTIN;

import java.util.function.Consumer;

import javax.persistence.EntityManager;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.test.fixtures.ActiveDriverFixture;
import com.rideaustin.test.fixtures.CarTypeFixture;
import com.rideaustin.test.fixtures.DriverFixture;
import com.rideaustin.test.fixtures.RideFixture;
import com.rideaustin.test.fixtures.RideTrackFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.check.DriverChecker;

public class RideFixtureProvider {

  private final EntityManager entityManager;
  private final DriverDslRepository driverDslRepository;
  private final RiderFixture riderFixture;
  private final DriverFixture driverFixture;
  private final CarTypeFixture carTypeFixture;
  private final RideTrackFixture rideTrackFixture;
  private final ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private final long cityId = AUSTIN.getId();

  public RideFixtureProvider(RiderFixture riderFixture, DriverFixture driverFixture, CarTypeFixture carTypeFixture,
    RideTrackFixture rideTrackFixture, ActiveDriverFixtureProvider activeDriverFixtureProvider,
    EntityManager entityManager, DriverDslRepository driverDslRepository) {
    this.riderFixture = riderFixture;
    this.driverFixture = driverFixture;
    this.carTypeFixture = carTypeFixture;
    this.rideTrackFixture = rideTrackFixture;
    this.activeDriverFixtureProvider = activeDriverFixtureProvider;
    this.entityManager = entityManager;
    this.driverDslRepository = driverDslRepository;
  }

  public RideFixture create(RideStatus status, boolean sameDriver) {
    Consumer<RideFixture.RideFixtureBuilder> postProcessor = builder -> {
    };
    if (status == RideStatus.COMPLETED) {
      postProcessor = builder -> builder.endLocation(new LatLng(0d, 0d));
    }
    return create(status, sameDriver, postProcessor);
  }

  public RideFixture create(RideStatus status, boolean sameDriver,
    Consumer<RideFixture.RideFixtureBuilder> postProcessor) {
    double surgeFactor = 1.0;
    RideFixture.RideFixtureBuilder fixtureBuilder = RideFixture.builder()
      .riderFixture(riderFixture)
      .activeDriverFixture(getActiveDriverFixture(driverFixture, sameDriver))
      .carTypeFixture(carTypeFixture)
      .rideTrackFixture(rideTrackFixture)
      .cityId(cityId)
      .status(status)
      .surgeFactor(surgeFactor)
      .tip(null);
    postProcessor.accept(fixtureBuilder);

    RideFixture fixture = fixtureBuilder.build();
    fixture.setEntityManager(entityManager);
    return fixture;
  }

  private ActiveDriverFixture getActiveDriverFixture(DriverFixture driverFixture, boolean sameDriver) {
    if (sameDriver) {
      driverFixture.setRecordChecker(new DriverChecker(driverDslRepository));
      ActiveDriverFixture activeDriverFixture = ActiveDriverFixture.builder()
        .status(ActiveDriverStatus.AVAILABLE)
        .driverFixture(driverFixture)
        .build();
      activeDriverFixture.setEntityManager(entityManager);
      return activeDriverFixture;
    } else {
      return activeDriverFixtureProvider.create();
    }
  }
}
