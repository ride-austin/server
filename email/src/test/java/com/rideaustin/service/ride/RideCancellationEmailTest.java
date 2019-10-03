package com.rideaustin.service.ride;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.BiConsumer;

import org.apache.commons.mail.EmailException;
import org.joda.money.Money;
import org.junit.Test;

import com.rideaustin.model.Address;
import com.rideaustin.model.City;
import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.RiderCardDto;
import com.rideaustin.service.model.FarePaymentDto;
import com.rideaustin.utils.DateUtils;

public class RideCancellationEmailTest extends BaseEmailTest {

  @Test
  public void modelContainsRideDate() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    final City city = setupCity();

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder().build());

    assertEquals("December 31, 2019", email.getModel().get("rideDate"));
  }

  @Test
  public void modelContainsTotalFare() throws EmailException {
    simpleFieldContainmentTest(FareDetails::setTotalFare, "totalFare");
  }

  @Test
  public void modelContainsStartLocation() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    final String address = "Austin";
    final Address startAddress = new Address();
    startAddress.setAddress(address);
    ride.setStart(startAddress);
    final City city = setupCity();

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder().build());

    assertEquals(address, email.getModel().get("startLocation"));
  }

  @Test
  public void modelContainsStartTimeForStartedRide() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    ride.setStartedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2019, 12, 31, 0, 0, 0), ZoneId.of("UTC")));
    final City city = setupCity();

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder().build());

    assertEquals("06:00PM", email.getModel().get("startTime"));
  }

  @Test
  public void modelContainsStartTimeForCancelledRide() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    ride.setStartedOn(null);
    final City city = setupCity();

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder().build());

    assertEquals("05:59PM", email.getModel().get("startTime"));
  }

  @Test
  public void modelContainsPaymentInformationForApplePayPaidRide() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    final City city = setupCity();

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder()
      .paymentStatus(PaymentStatus.PAID)
      .paymentProvider(PaymentProvider.APPLE_PAY)
      .build());

    final String cardImage = (String) email.getModel().get("cardImage");
    final String cardBrand = (String) email.getModel().get("cardBrand");
    final String cardNickName = (String) email.getModel().get("cardNickName");
    final String cardNumber = (String) email.getModel().get("cardNumber");
    assertEquals(PaymentProvider.APPLE_PAY.getIcon(), cardImage);
    assertEquals(PaymentProvider.APPLE_PAY.getName(), cardBrand);
    assertEquals("", cardNickName);
    assertEquals("", cardNumber);
  }

  @Test
  public void modelContainsPaymentInformationForCCPaidRide() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    final City city = setupCity();

    final CardBrand expectedCardBrand = CardBrand.VISA;
    final String expectedCardNumber = "9999";

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder()
      .paymentStatus(PaymentStatus.PAID)
      .paymentProvider(PaymentProvider.CREDIT_CARD)
      .usedCard(RiderCardDto.builder()
        .cardBrand(expectedCardBrand)
        .cardNumber(expectedCardNumber)
        .build()
      )
      .build());

    final String cardImage = (String) email.getModel().get("cardImage");
    final String cardBrand = (String) email.getModel().get("cardBrand");
    final String cardNickName = (String) email.getModel().get("cardNickName");
    final String cardNumber = (String) email.getModel().get("cardNumber");
    assertEquals(expectedCardBrand.imageURL(), cardImage);
    assertEquals(expectedCardBrand.name(), cardBrand);
    assertEquals("Personal", cardNickName);
    assertEquals("&#x25CF;&#x25CF;&#x25CF;&#x25CF;"+expectedCardNumber, cardNumber);
  }

  @Test
  public void modelContainsNoChargePaymentInformation() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    final City city = setupCity();

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder()
      .paymentStatus(PaymentStatus.UNPAID)
      .build());

    final String cardImage = (String) email.getModel().get("cardImage");
    final String cardBrand = (String) email.getModel().get("cardBrand");
    final String cardNickName = (String) email.getModel().get("cardNickName");
    final String cardNumber = (String) email.getModel().get("cardNumber");
    assertEquals(CardBrand.VISA.imageURL(), cardImage);
    assertEquals("", cardBrand);
    assertEquals("", cardNickName);
    assertEquals("No charge", cardNumber);
  }

  @Test
  public void modelContainsHasPromocodeFlagWhenFreeCreditChargeIsPositive() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setFreeCreditCharged(money(10.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    final City city = setupCity();

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder().build());

    final Boolean hasPromocode = (Boolean) email.getModel().get("hasPromocode");
    assertTrue(hasPromocode);
  }

  @Test
  public void modelContainsFreeCreditCharged() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setFreeCreditCharged(money(10.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    final City city = setupCity();

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder().build());

    assertEquals("10.00", email.getModel().get("freeCreditCharge"));
  }

  @Test
  public void modelContainsStripeCreditCharge() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setStripeCreditCharge(money(10.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    final City city = setupCity();

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder().build());

    assertEquals("10.00", email.getModel().get("stripeCreditCharge"));
  }

  @Test
  public void modelContainsCityObject() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    final City city = setupCity();

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder().build());

    assertEquals(city, email.getModel().get("city"));
  }

  private void simpleFieldContainmentTest(BiConsumer<FareDetails, Money> consumer, final String fieldKey) throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    consumer.accept(fareDetails, money(4.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Rider rider = setupRider();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setRider(rider);
    final City city = setupCity();

    final RideCancellationEmail email = new RideCancellationEmail(ride, new byte[0], city, FarePaymentDto.builder().build());

    final String fare = (String) email.getModel().get(fieldKey);
    assertEquals("4.00", fare);
  }
}