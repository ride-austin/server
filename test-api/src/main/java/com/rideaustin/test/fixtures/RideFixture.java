package com.rideaustin.test.fixtures;

import static com.rideaustin.test.util.TestUtils.money;
import static com.rideaustin.test.util.TestUtils.moneyOrNull;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Address;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideDriverDispatch;
import com.rideaustin.utils.RandomString;

public class RideFixture extends AbstractFixture<Ride> {

  private final DispatchFixture dispatchFixture;
  private final LatLng endLocation;
  private RiderFixture riderFixture;
  private ActiveDriverFixture activeDriverFixture;
  private CarTypeFixture carTypeFixture;
  private RideTrackFixture rideTrackFixture;
  private SessionFixture riderSessionFixture;
  private RideStatus status;
  private Double tip;
  private Double totalFare;
  private long cityId;
  private double surgeFactor;

  private ActiveDriver fixedActiveDriver;

  RideFixture(RiderFixture riderFixture, ActiveDriverFixture activeDriverFixture, CarTypeFixture carTypeFixture,
    RideStatus status, Double tip, Double totalFare, long cityId, double surgeFactor, RideTrackFixture rideTrackFixture,
    DispatchFixture dispatchFixture, LatLng endLocation) {
    this.riderFixture = riderFixture;
    this.activeDriverFixture = activeDriverFixture;
    this.carTypeFixture = carTypeFixture;
    this.status = status;
    this.tip = tip;
    this.totalFare = totalFare;
    this.cityId = cityId;
    this.surgeFactor = surgeFactor;
    this.rideTrackFixture = rideTrackFixture;
    this.dispatchFixture = dispatchFixture;
    this.endLocation = endLocation;
  }

  public static RideFixtureBuilder builder() {
    return new RideFixtureBuilder();
  }

  @Override
  protected Ride createObject() {
    Date initialDate = new Date();
    Ride ride = Ride.builder().fareDetails(createFareDetails(totalFare, tip))
      .rider(riderFixture.getFixture())
      .requestedCarType(carTypeFixture.getFixture())
      .status(status)
      .cityId(cityId)
      .startLocationLat(30.240549)
      .startLocationLong(-97.785888)
      .distanceTravelled(BigDecimal.valueOf(1870L))
      .requestedOn(initialDate)
      .driverAcceptedOn(initialDate)
      .driverReachedOn(initialDate)
      .startedOn(initialDate)
      .completedOn(DateUtils.addSeconds(initialDate, 17))
      .start(new Address(RandomString.generate(10), RandomString.generate(5)))
      .surgeFactor(BigDecimal.valueOf(surgeFactor))
      .build();
    if (activeDriverFixture != null) {
      fixedActiveDriver = activeDriverFixture.getFixture();
      ride.setActiveDriver(fixedActiveDriver);
    }
    if (riderSessionFixture != null) {
      ride.setRiderSession(riderSessionFixture.getFixture());
    }
    if (endLocation != null) {
      ride.setEndLocationLat(endLocation.lat);
      ride.setEndLocationLong(endLocation.lng);
    }
    return ride;
  }

  @Override
  public Ride getFixture() {
    Ride ride = super.getFixture();
    if (rideTrackFixture != null) {
      rideTrackFixture.setRideId(ride.getId());
      rideTrackFixture.getFixture();
    }
    if (dispatchFixture != null) {
      dispatchFixture.setRide(ride);
      RideDriverDispatch dispatch = dispatchFixture.getFixture();
      fixedActiveDriver = dispatch.getActiveDriver();
    }
    return ride;
  }

  public ActiveDriver getActiveDriver() {
    if (!isFixed()) {
      throw new FixtureException();
    }
    return fixedActiveDriver;
  }

  private FareDetails createFareDetails(double totalFare, Double tip) {
    return FareDetails.builder()
      .baseFare(money(0.0))
      .bookingFee(money(0.0))
      .cityFee(money(0.0))
      .subTotal(money(0.0))
      .distanceFare(money(0.0))
      .minimumFare(money(0.0))
      .ratePerMile(money(0.0))
      .ratePerMinute(money(0.0))
      .roundUpAmount(money(0.0))
      .timeFare(money(0.0))
      .totalFare(money(totalFare))
      .driverPayment(money(0.0))
      .freeCreditCharged(money(0.0))
      .stripeCreditCharge(money(0.0))
      .cancellationFee(money(0.0))
      .surgeFare(money(0.0))
      .normalFare(money(0.0))
      .tip(moneyOrNull(tip))
      .raPayment(money(0.0))
      .processingFee(money(0.0))
      .airportFee(money(0.0))
      .build();
  }

  public void setRiderSessionFixture(SessionFixture riderSessionFixture) {
    this.riderSessionFixture = riderSessionFixture;
  }

  public static class RideFixtureBuilder {
    private RiderFixture riderFixture;
    private ActiveDriverFixture activeDriverFixture;
    private CarTypeFixture carTypeFixture;
    private RideTrackFixture rideTrackFixture;
    private DispatchFixture dispatchFixture;
    private RideStatus status;
    private Double tip;
    private long cityId;
    private double surgeFactor;
    private Double totalFare = 0.0;
    private LatLng endLocation;

    public RideFixtureBuilder riderFixture(RiderFixture riderFixture) {
      this.riderFixture = riderFixture;
      return this;
    }

    public RideFixtureBuilder activeDriverFixture(ActiveDriverFixture activeDriverFixture) {
      this.activeDriverFixture = activeDriverFixture;
      return this;
    }

    public RideFixtureBuilder carTypeFixture(CarTypeFixture carTypeFixture) {
      this.carTypeFixture = carTypeFixture;
      return this;
    }

    public RideFixtureBuilder rideTrackFixture(RideTrackFixture rideTrackFixture) {
      this.rideTrackFixture = rideTrackFixture;
      return this;
    }

    public RideFixtureBuilder status(RideStatus status) {
      this.status = status;
      return this;
    }

    public RideFixtureBuilder tip(Double tip) {
      this.tip = tip;
      return this;
    }

    public RideFixtureBuilder totalFare(Double totalFare) {
      this.totalFare = totalFare;
      return this;
    }

    public RideFixtureBuilder cityId(long cityId) {
      this.cityId = cityId;
      return this;
    }

    public RideFixtureBuilder surgeFactor(double surgeFactor) {
      this.surgeFactor = surgeFactor;
      return this;
    }

    public RideFixtureBuilder dispatchFixture(DispatchFixture dispatchFixture) {
      this.dispatchFixture = dispatchFixture;
      return this;
    }

    public RideFixtureBuilder endLocation(LatLng location) {
      this.endLocation = location;
      return this;
    }

    public RideFixture build() {
      return new RideFixture(riderFixture, activeDriverFixture, carTypeFixture, status, tip, totalFare, cityId,
        surgeFactor, rideTrackFixture, dispatchFixture, endLocation);
    }
  }
}
