package com.rideaustin.test.fixtures;

import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideDriverDispatch;

public class DispatchFixture extends AbstractFixture<RideDriverDispatch> {

  private ActiveDriverFixture activeDriverFixture;
  private Ride ride;

  @java.beans.ConstructorProperties({"activeDriverFixture"})
  DispatchFixture(ActiveDriverFixture activeDriverFixture) {
    this.activeDriverFixture = activeDriverFixture;
  }

  public static DispatchFixtureBuilder builder() {
    return new DispatchFixtureBuilder();
  }

  @Override
  protected RideDriverDispatch createObject() {
    RideDriverDispatch dispatch = RideDriverDispatch.builder()
      .activeDriver(activeDriverFixture.getFixture())
      .status(DispatchStatus.DISPATCHED)
      .build();
    dispatch.setRide(ride);
    return dispatch;
  }

  public void setRide(Ride ride) {
    this.ride = ride;
  }

  public static class DispatchFixtureBuilder {
    private ActiveDriverFixture activeDriverFixture;

    DispatchFixtureBuilder() {
    }

    public DispatchFixture.DispatchFixtureBuilder activeDriverFixture(ActiveDriverFixture activeDriverFixture) {
      this.activeDriverFixture = activeDriverFixture;
      return this;
    }

    public DispatchFixture build() {
      return new DispatchFixture(activeDriverFixture);
    }

    public String toString() {
      return "com.rideaustin.test.fixtures.DispatchFixture.DispatchFixtureBuilder(activeDriverFixture=" + this.activeDriverFixture + ")";
    }
  }
}
