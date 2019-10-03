package com.rideaustin.test.fixtures;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.promocodes.PromocodeRedemption;

public class PromocodeRedemptionFixture extends AbstractFixture<PromocodeRedemption> {

  private PromocodeFixture promocodeFixture;
  private RiderFixture riderFixture;
  private boolean active;
  private boolean valid;

  @java.beans.ConstructorProperties({"promocodeFixture", "riderFixture", "active", "valid"})
  PromocodeRedemptionFixture(PromocodeFixture promocodeFixture, RiderFixture riderFixture, boolean active, boolean valid) {
    this.promocodeFixture = promocodeFixture;
    this.riderFixture = riderFixture;
    this.active = active;
    this.valid = valid;
  }

  public static PromocodeRedemptionFixtureBuilder builder() {
    return new PromocodeRedemptionFixtureBuilder();
  }

  @Override
  protected PromocodeRedemption createObject() {
    Promocode promocode = promocodeFixture.getFixture();
    PromocodeRedemption redemption = PromocodeRedemption.builder()
      .promocode(promocode)
      .rider(riderFixture.getFixture())
      .active(active)
      .numberOfTimesUsed(0)
      .originalValue(promocode.getCodeValue())
      .remainingValue(promocode.getCodeValue())
      .build();
    if (valid) {
      redemption.setValidUntil(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    } else {
      redemption.setValidUntil(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
    }
    return redemption;
  }

  public static class PromocodeRedemptionFixtureBuilder {
    private PromocodeFixture promocodeFixture;
    private RiderFixture riderFixture;
    private boolean active;
    private boolean valid;

    public PromocodeRedemptionFixture.PromocodeRedemptionFixtureBuilder promocodeFixture(PromocodeFixture promocodeFixture) {
      this.promocodeFixture = promocodeFixture;
      return this;
    }

    public PromocodeRedemptionFixture.PromocodeRedemptionFixtureBuilder riderFixture(RiderFixture riderFixture) {
      this.riderFixture = riderFixture;
      return this;
    }

    public PromocodeRedemptionFixture.PromocodeRedemptionFixtureBuilder active(boolean active) {
      this.active = active;
      return this;
    }

    public PromocodeRedemptionFixture.PromocodeRedemptionFixtureBuilder valid(boolean valid) {
      this.valid = valid;
      return this;
    }

    public PromocodeRedemptionFixture build() {
      return new PromocodeRedemptionFixture(promocodeFixture, riderFixture, active, valid);
    }

    public String toString() {
      return "com.rideaustin.test.fixtures.PromocodeRedemptionFixture.PromocodeRedemptionFixtureBuilder(promocodeFixture=" + this.promocodeFixture + ", riderFixture=" + this.riderFixture + ", active=" + this.active + ", valid=" + this.valid + ")";
    }
  }
}
