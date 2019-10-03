package com.rideaustin.service.ride;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.function.BiConsumer;

import org.apache.commons.mail.EmailException;
import org.joda.money.Money;
import org.junit.Test;

import com.rideaustin.Constants;
import com.rideaustin.model.Address;
import com.rideaustin.model.Charity;
import com.rideaustin.model.City;
import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RiderOverride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.RiderCardDto;
import com.rideaustin.service.model.CampaignDto;
import com.rideaustin.service.model.FarePaymentDto;
import com.rideaustin.utils.DateUtils;

public class EndRideEmailTest extends BaseEmailTest {

  @Test
  public void modelContainsFare() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String fare = (String) email.getModel().get("fare");
    assertEquals("10.00", fare);
  }

  @Test
  public void modelContainsRideId() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setId(1L);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String modelRideId = (String) email.getModel().get("rideId");
    assertEquals("1", modelRideId);
  }

  @Test
  public void modelContainsCreatedDate() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String rideDate = (String) email.getModel().get("rideDate");
    assertEquals("December 31, 2019", rideDate);
  }

  @Test
  public void modelContainsRiderName() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String riderName = (String) email.getModel().get("riderName");
    assertEquals("You", riderName);
  }

  @Test
  public void modelContainsOverriddenName() throws EmailException {
    final ActiveDriver activeDriver = setupActiveDriver();
    final FareDetails fareDetails = setupFareDetails();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final RiderOverride riderOverride = new RiderOverride();
    riderOverride.setFirstName("RA");
    riderOverride.setLastName("RB");
    ride.setRiderOverride(riderOverride);
    final City city = setupCity();
    final Rider rider = setupRider();
    rider.setDispatcherAccount(true);

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String riderName = (String) email.getModel().get("riderName");
    assertEquals("RA RB", riderName);
  }

  @Test
  public void modelContainsBaseFare() throws EmailException {
    simpleFieldContainmentTest(FareDetails::setBaseFare, "baseFare");
  }

  @Test
  public void modelContainsDistanceFare() throws EmailException {
    simpleFieldContainmentTest(FareDetails::setDistanceFare, "distanceFare");
  }

  @Test
  public void modelContainsTimeFare() throws EmailException {
    simpleFieldContainmentTest(FareDetails::setTimeFare, "timeFare");
  }

  @Test
  public void modelContainsTotalFare() throws EmailException {
    simpleFieldContainmentTest(FareDetails::setTotalFare, "totalFare");
  }

  @Test
  public void modelContainsSubTotal() throws EmailException {
    simpleFieldContainmentTest(FareDetails::setSubTotal, "subTotal");
  }

  @Test
  public void modelContainsTrueMinimumFareFlagWhenSubtotalEqualsToMinimum() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setSubTotal(money(4.0));
    fareDetails.setMinimumFare(money(4.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final Boolean hasOnlyMinimumFare = (Boolean) email.getModel().get("hasOnlyMinimumFare");
    assertTrue(hasOnlyMinimumFare);
  }

  @Test
  public void modelContainsTrueMinimumFareFlagWhenSubtotalLessThanMinimum() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setSubTotal(money(3.0));
    fareDetails.setMinimumFare(money(4.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final Boolean hasOnlyMinimumFare = (Boolean) email.getModel().get("hasOnlyMinimumFare");
    assertTrue(hasOnlyMinimumFare);
  }

  @Test
  public void modelContainsFalseMinimumFareFlagWhenSubtotalLessThanMinimum() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setSubTotal(money(5.0));
    fareDetails.setMinimumFare(money(4.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final Boolean hasOnlyMinimumFare = (Boolean) email.getModel().get("hasOnlyMinimumFare");
    assertFalse(hasOnlyMinimumFare);
  }

  @Test
  public void modelContainsBookingFeeFlagWhenBookingFeeIsPositive() throws EmailException {
    conditionalFeePositiveTest(FareDetails::setBookingFee, "hasBookingFee", "bookingFee");
  }

  @Test
  public void modelContainsBookingFeeFlagWhenBookingFeeIsZero() throws EmailException {
    conditionalFeeZeroTest(FareDetails::setBookingFee, "hasBookingFee", "bookingFee");
  }

  @Test
  public void modelContainsAirportFeeFlagWhenAirportFeeIsPositive() throws EmailException {
    conditionalFeePositiveTest(FareDetails::setAirportFee, "hasAirportFee", "airportFee");
  }

  @Test
  public void modelContainsAirportFeeFlagWhenAirportFeeIsZero() throws EmailException {
    conditionalFeeZeroTest(FareDetails::setAirportFee, "hasAirportFee", "airportFee");
  }

  @Test
  public void modelContainsCityFee() throws EmailException {
    simpleFieldContainmentTest(FareDetails::setCityFee, "cityFee");
  }

  @Test
  public void modelContainsRideCost() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setCityFee(money(1.0));
    fareDetails.setBookingFee(money(2.0));
    fareDetails.setSubTotal(money(3.0));
    fareDetails.setProcessingFee(money(0.5));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String fare = (String) email.getModel().get("rideCost");
    assertEquals("6.50", fare);
  }

  @Test
  public void modelContainsProcessingFeeFlagWhenProcessingFeeIsPositive() throws EmailException {
    conditionalFeePositiveTest(FareDetails::setProcessingFee, "hasProcessingFee", "processingFee");
  }

  @Test
  public void modelContainsProcessingFeeFlagWhenProcessingFeeIsZero() throws EmailException {
    conditionalFeeZeroTest(FareDetails::setProcessingFee, "hasProcessingFee", "processingFee");
  }

  @Test
  public void modelContainsStartTime() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setStartedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2019, 12, 31, 23, 58, 0), Constants.CST_ZONE));
    ride.setCompletedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2019, 12, 31, 23, 59, 59), Constants.CST_ZONE));
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String startTime = (String) email.getModel().get("startTime");
    assertEquals("11:58PM", startTime);
  }

  @Test
  public void modelContainsEndTime() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setStartedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2019, 12, 31, 23, 58, 0), Constants.CST_ZONE));
    ride.setCompletedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2019, 12, 31, 23, 59, 59), Constants.CST_ZONE));
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String endTime = (String) email.getModel().get("endTime");
    assertEquals("11:59PM", endTime);
  }

  @Test
  public void modelContainsStartLocationAddress() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final Address startAddress = new Address();
    final String address = "Address";
    startAddress.setAddress(address);
    ride.setStart(startAddress);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String startLocation = (String) email.getModel().get("startLocation");
    assertEquals(address, startLocation);
  }

  @Test
  public void modelContainsStartLocationCoordinates() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setStartLocationLat(34.123);
    ride.setStartLocationLong(-97.321);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String startLocation = (String) email.getModel().get("startLocation");
    assertEquals("34.123 -97.321", startLocation);
  }

  @Test
  public void modelContainsEndLocationAddress() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final Address endAddress = new Address();
    final String address = "Address";
    endAddress.setAddress(address);
    ride.setEnd(endAddress);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String endLocation = (String) email.getModel().get("endLocation");
    assertEquals(address, endLocation);
  }

  @Test
  public void modelContainsEndLocationCoordinates() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setEndLocationLat(34.123);
    ride.setEndLocationLong(-97.321);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String endLocation = (String) email.getModel().get("endLocation");
    assertEquals("34.123 -97.321", endLocation);
  }

  @Test
  public void modelContainsCarCategory() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final String carCategoryTitle = "REGULAR";
    final CarType requestedCarType = new CarType();
    requestedCarType.setTitle(carCategoryTitle);
    ride.setRequestedCarType(requestedCarType);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String car = (String) email.getModel().get("car");
    assertEquals(carCategoryTitle, car);
  }

  @Test
  public void modelContainsTripTime() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setStartedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2019, 12, 31, 23, 58, 0), Constants.CST_ZONE));
    ride.setCompletedOn(DateUtils.localDateTimeToDate(LocalDateTime.of(2019, 12, 31, 23, 59, 59), Constants.CST_ZONE));
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String tripTime = (String) email.getModel().get("tripTime");
    assertEquals("1m 59s", tripTime);
  }

  @Test
  public void modelContainsDistanceTravelled() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setDistanceTravelled(BigDecimal.valueOf(1609.34));
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String distanceTravelled = (String) email.getModel().get("distanceTravelled");
    assertEquals("1.00", distanceTravelled);
  }

  @Test
  public void modelContainsDriverNickNameIfPresent() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final String driverNickname = "ABC";
    activeDriver.getDriver().getUser().setNickName(driverNickname);
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String modelNickName = (String) email.getModel().get("driverNickName");
    assertEquals(driverNickname, modelNickName);
  }

  @Test
  public void modelContainsDriverFirstNameIfNickIsMissing() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final String driverFirstname = "ABC";
    activeDriver.getDriver().getUser().setFirstname(driverFirstname);
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String modelNickName = (String) email.getModel().get("driverNickName");
    assertEquals(driverFirstname, modelNickName);
  }

  @Test
  public void modelContainsDriverPhotoIfPresent() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      driverPhoto, null);

    final String modelDriverPhoto = (String) email.getModel().get("driverPhoto");
    assertEquals(driverPhoto, modelDriverPhoto);
  }

  @Test
  public void modelContainsDefaultDriverPhoto() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      null, null);

    final String modelDriverPhoto = (String) email.getModel().get("driverPhoto");
    assertEquals(Constants.DEFAULT_DRIVER_PHOTO, modelDriverPhoto);
  }

  @Test
  public void modelContainsSurgeFactorFlagIfFactorIsMoreThanOne() throws EmailException {
    final EndRideEmail email = doSurgeFareTest(BigDecimal.valueOf(2.0), null);

    final Boolean hasSurge = (Boolean) email.getModel().get("hasSurge");
    assertTrue(hasSurge);
    assertEquals("2.00", email.getModel().get("surgeFactor"));
  }

  @Test
  public void modelContainsNormalFareIfSurgeFactorMoreThanOne() throws EmailException {
    final EndRideEmail email = doSurgeFareTest(BigDecimal.valueOf(2.0), FareDetails::setNormalFare);

    final String normalFare = (String) email.getModel().get("normalFare");

    assertEquals("1.00", normalFare);
  }

  @Test
  public void modelContainsSurgeFareIfSurgeFactorMoreThanOne() throws EmailException {
    final EndRideEmail email = doSurgeFareTest(BigDecimal.valueOf(2.0), FareDetails::setSurgeFare);

    final String surgeFare = (String) email.getModel().get("surgeFare");

    assertEquals("1.00", surgeFare);
  }

  @Test
  public void modelContainsSurgeFactorFlagIfFactorIsOne() throws EmailException {
    final EndRideEmail email = doSurgeFareTest(BigDecimal.ONE, null);

    final Boolean hasSurge = (Boolean) email.getModel().get("hasSurge");
    assertFalse(hasSurge);
    assertFalse(email.getModel().containsKey("normalFare"));
    assertFalse(email.getModel().containsKey("surgeFare"));
    assertFalse(email.getModel().containsKey("surgeFactor"));
  }

  @Test
  public void modelContainsSurgeFactorFlagIfFactorIsNull() throws EmailException {
    final EndRideEmail email = doSurgeFareTest(null, null);

    final Boolean hasSurge = (Boolean) email.getModel().get("hasSurge");
    assertFalse(hasSurge);
    assertFalse(email.getModel().containsKey("normalFare"));
    assertFalse(email.getModel().containsKey("surgeFare"));
    assertFalse(email.getModel().containsKey("surgeFactor"));
  }

  @Test
  public void modelContainsPaymentInformationForApplePayPaidRide() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder()
      .paymentStatus(PaymentStatus.PAID)
      .paymentProvider(PaymentProvider.APPLE_PAY)
      .build(), new byte[0], driverPhoto, null);

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
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final CardBrand expectedCardBrand = CardBrand.VISA;
    final String expectedCardNumber = "9999";
    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder()
      .paymentStatus(PaymentStatus.PAID)
      .paymentProvider(PaymentProvider.CREDIT_CARD)
      .usedCard(RiderCardDto.builder()
        .cardBrand(expectedCardBrand)
        .cardNumber(expectedCardNumber)
        .build()
      )
      .build(), new byte[0], driverPhoto, null);

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
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder()
      .paymentStatus(PaymentStatus.UNPAID)
      .build(), new byte[0], driverPhoto, null);

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
  public void modelContainsHasCharityFlagWhenCharityIsPresentAndRoundupIsPositive() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setRoundUpAmount(money(.25));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final Charity charity = new Charity();
    final String imageUrl = "imageUrl";
    final String charityName = "charityName";
    charity.setImageUrl(imageUrl);
    charity.setName(charityName);
    ride.setCharity(charity);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0], driverPhoto, null);

    final Boolean hasCharity = (Boolean) email.getModel().get("hasCharity");
    assertTrue(hasCharity);
    assertEquals("0.25", email.getModel().get("roundUpFare"));
    assertEquals(imageUrl, email.getModel().get("charityLogo"));
    assertEquals(charityName, email.getModel().get("charityName"));
  }

  @Test
  public void modelContainsHasCharityFlagWhenCharityIsNotPresent() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setRoundUpAmount(money(.25));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0], driverPhoto, null);

    final Boolean hasCharity = (Boolean) email.getModel().get("hasCharity");
    assertFalse(hasCharity);
    assertFalse(email.getModel().containsKey("roundUpFare"));
    assertFalse(email.getModel().containsKey("charityLogo"));
    assertFalse(email.getModel().containsKey("charityName"));
  }

  @Test
  public void modelContainsHasCharityFlagWhenCharityIsPresentAndRoundUpIsZero() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setRoundUpAmount(money(0.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setCharity(new Charity());
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0], driverPhoto, null);

    final Boolean hasCharity = (Boolean) email.getModel().get("hasCharity");
    assertFalse(hasCharity);
    assertFalse(email.getModel().containsKey("roundUpFare"));
    assertFalse(email.getModel().containsKey("charityLogo"));
    assertFalse(email.getModel().containsKey("charityName"));
  }

  @Test
  public void modelContainsHasTippedFlagForTippedRideAndMainRider() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setTip(money(10.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder()
      .mainRider(true)
      .build(), new byte[0], driverPhoto, null);

    final Boolean isTipped = (Boolean) email.getModel().get("isTipped");
    assertTrue(isTipped);
    assertEquals("10.00", email.getModel().get("tip"));
  }

  @Test
  public void modelContainsHasTippedFlagForNotTippedRideAndMainRider() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setTip(null);
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder()
      .mainRider(true)
      .build(), new byte[0], driverPhoto, null);

    final Boolean isTipped = (Boolean) email.getModel().get("isTipped");
    assertFalse(isTipped);
    assertFalse(email.getModel().containsKey("tip"));
  }

  @Test
  public void modelContainsHasTippedFlagForTippedRideAndNotMainRider() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setTip(money(10.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder()
      .mainRider(false)
      .build(), new byte[0], driverPhoto, null);

    final Boolean isTipped = (Boolean) email.getModel().get("isTipped");
    assertFalse(isTipped);
    assertFalse(email.getModel().containsKey("tip"));
  }

  @Test
  public void modelContainsHasPromocodeFlagWhenFreeCreditChargeIsPositive() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder()
      .freeCreditCharged(money(10.0))
      .build(), new byte[0], driverPhoto, null);

    final Boolean hasPromocode = (Boolean) email.getModel().get("hasPromocode");
    assertTrue(hasPromocode);
  }

  @Test
  public void modelContainsHasPromocodeFlagWhenFreeCreditChargeIsZero() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder()
      .freeCreditCharged(money(0.0))
      .build(), new byte[0], driverPhoto, null);

    final Boolean hasPromocode = (Boolean) email.getModel().get("hasPromocode");
    assertFalse(hasPromocode);
  }

  @Test
  public void modelContainsHasRideCreditFlagWhenPromocodeIsPresentAndApplicableToFees() throws EmailException {
    final Money freeCreditCharged = money(10.0);
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setFreeCreditCharged(freeCreditCharged);
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";
    final Promocode promocode = new Promocode();
    promocode.setApplicableToFees(true);

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().freeCreditCharged(freeCreditCharged).build(),
      new ArrayList<>(), new byte[0], promocode, driverPhoto, null);

    final Boolean hasRideCredit = (Boolean) email.getModel().get("hasRideCredit");
    assertTrue(hasRideCredit);
    assertEquals("10.00", email.getModel().get("freeCreditCharge"));
  }

  @Test
  public void modelContainsHasRideCreditFlagWhenPromocodeIsAbsent() throws EmailException {
    final Money freeCreditCharged = money(10.0);
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setFreeCreditCharged(freeCreditCharged);
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().freeCreditCharged(freeCreditCharged).build(),
      new ArrayList<>(), new byte[0], null, driverPhoto, null);

    final Boolean hasRideCredit = (Boolean) email.getModel().get("hasRideCredit");
    assertFalse(hasRideCredit);
    assertFalse(email.getModel().containsKey("freeCreditCharge"));
  }

  @Test
  public void modelContainsHasRideCreditFlagWhenPromocodeIsNotApplicableToFees() throws EmailException {
    final Money freeCreditCharged = money(10.0);
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setFreeCreditCharged(freeCreditCharged);
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";
    final Promocode promocode = new Promocode();
    promocode.setApplicableToFees(false);

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().freeCreditCharged(freeCreditCharged).build(),
      new ArrayList<>(), new byte[0], promocode, driverPhoto, null);

    final Boolean hasRideCredit = (Boolean) email.getModel().get("hasRideCredit");
    final Boolean hasFareCredit = (Boolean) email.getModel().get("hasFareCredit");
    assertFalse(hasRideCredit);
    assertTrue(hasFareCredit);
    assertEquals("10.00", email.getModel().get("freeCreditCharge"));
  }

  @Test
  public void modelContainsHasRideCreditFlagWhenFreeCreditChargeIsZero() throws EmailException {
    final Money freeCreditCharged = money(0.0);
    final FareDetails fareDetails = setupFareDetails();
    fareDetails.setFreeCreditCharged(freeCreditCharged);
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";
    final Promocode promocode = new Promocode();
    promocode.setApplicableToFees(true);

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().freeCreditCharged(freeCreditCharged).build(),
      new ArrayList<>(), new byte[0], promocode, driverPhoto, null);

    final Boolean hasRideCredit = (Boolean) email.getModel().get("hasRideCredit");
    assertFalse(hasRideCredit);
    assertFalse(email.getModel().containsKey("freeCreditCharge"));
  }

  @Test
  public void modelContainsPromocodeTitle() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";
    final Promocode promocode = new Promocode();
    final String promocodeTitle = "AZAZA";
    promocode.setTitle(promocodeTitle);

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(),
      new ArrayList<>(), new byte[0], promocode, driverPhoto, null);

    final String promoTitle = (String) email.getModel().get("promoTitle");
    assertEquals(promocodeTitle, promoTitle);
  }

  @Test
  public void modelContainsIsCampaignRideFlag() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";
    final CampaignDto campaign = new CampaignDto("Campaign", money(10.0), "header");

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      driverPhoto, campaign);

    final Boolean isCampaignRide = (Boolean) email.getModel().get("isCampaignRide");
    assertTrue(isCampaignRide);
    assertEquals(campaign.getName(), email.getModel().get("campaignName"));
    assertEquals(campaign.getHeaderImage(), email.getModel().get("campaignImage"));
    assertEquals("10.00", email.getModel().get("campaignCoverage"));
  }

  @Test
  public void modelContainsIsCampaignRideFlagWhenNoCampaignIsPresent() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      driverPhoto, null);

    final Boolean isCampaignRide = (Boolean) email.getModel().get("isCampaignRide");
    assertFalse(isCampaignRide);
    assertFalse(email.getModel().containsKey("campaignName"));
    assertFalse(email.getModel().containsKey("campaignImage"));
    assertFalse(email.getModel().containsKey("campaignCoverage"));
  }

  @Test
  public void modelContainsStripeCreditCharge() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder()
      .stripeCreditCharge(money(10.0))
      .build(), new byte[0],
      driverPhoto, null);

    assertEquals("10.00", email.getModel().get("stripeCreditCharge"));
  }

  @Test
  public void modelContainsCityObject() throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder()
      .stripeCreditCharge(money(10.0))
      .build(), new byte[0],
      driverPhoto, null);

    assertEquals(city, email.getModel().get("city"));
  }


  private EndRideEmail doSurgeFareTest(BigDecimal surgeFactor, BiConsumer<FareDetails, Money> consumer) throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    ride.setSurgeFactor(surgeFactor);
    if (consumer != null) {
      consumer.accept(fareDetails, money(1.0));
    }
    final City city = setupCity();
    final Rider rider = setupRider();
    final String driverPhoto = "ABC";

    return new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      driverPhoto, null);
  }

  private void simpleFieldContainmentTest(BiConsumer<FareDetails, Money> consumer, final String fieldKey) throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    consumer.accept(fareDetails, money(4.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final String fare = (String) email.getModel().get(fieldKey);
    assertEquals("4.00", fare);
  }

  private void conditionalFeePositiveTest(final BiConsumer<FareDetails, Money> consumer, final String flagKey, final String feeKey) throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    consumer.accept(fareDetails, money(5.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final Boolean hasFeeFlag = (Boolean) email.getModel().get(flagKey);
    assertTrue(hasFeeFlag);
    assertEquals("5.00", email.getModel().get(feeKey));
  }

  private void conditionalFeeZeroTest(final BiConsumer<FareDetails, Money> consumer, final String flagKey, final String feeKey) throws EmailException {
    final FareDetails fareDetails = setupFareDetails();
    consumer.accept(fareDetails, money(0.0));
    final ActiveDriver activeDriver = setupActiveDriver();
    final Ride ride = setupRide(fareDetails, activeDriver);
    final City city = setupCity();
    final Rider rider = setupRider();

    final EndRideEmail email = new EndRideEmail(ride, rider, city, FarePaymentDto.builder().build(), new byte[0],
      "ABC", null);

    final Boolean hasFlag = (Boolean) email.getModel().get(flagKey);
    assertFalse(hasFlag);
    assertFalse(email.getModel().containsKey(feeKey));
  }
}