package com.rideaustin.test.fixtures;

import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.utils.RandomString;

public class CardFixture extends AbstractFixture<RiderCard> {

  private boolean cardExpired;
  private Rider rider;

  CardFixture(boolean cardExpired) {
    this.cardExpired = cardExpired;
  }

  public static CardFixtureBuilder builder() {
    return new CardFixtureBuilder();
  }

  @Override
  protected RiderCard createObject() {
    return RiderCard.builder()
      .cardExpired(cardExpired)
      .stripeCardId(RandomString.generate())
      .rider(rider)
      .cardBrand(CardBrand.VISA)
      .cardNumber(RandomString.generate("1234567890", 4))
      .fingerprint(RandomString.generate())
      .build();
  }

  public void setRider(Rider rider) {
    this.rider = rider;
  }

  public static class CardFixtureBuilder {
    private boolean cardExpired;

    public CardFixtureBuilder cardExpired(boolean cardExpired) {
      this.cardExpired = cardExpired;
      return this;
    }

    public CardFixture build() {
      return new CardFixture(cardExpired);
    }

  }
}
