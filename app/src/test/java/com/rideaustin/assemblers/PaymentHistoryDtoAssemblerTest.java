package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.Address;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.PaymentHistoryDto;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.thirdparty.S3StorageService;

@RunWith(MockitoJUnitRunner.class)
public class PaymentHistoryDtoAssemblerTest {

  private static final long RIDE_ID = 4312L;
  private static final Long RIDER_ID = 1L;
  private static final long FARE_PAYMENT_ID = 1234L;
  private static final int STARTED_OFFSET = 1;
  private static final int COMPLETED_OFFSET = 2;
  private static final int CANCEL_OFFSET = 3;
  private static final Double DRIVER_RATING = 4d;
  private static final Long CARD_ID = 2L;
  private static final String CARD_NUMBER = "12334565678";
  private static final String DRIVER_FIRSTNAME = "D_FIRSTNAME";
  private static final String DRIVER_LASTNAME = "D_LASTNAME";
  private static final String DRIVER_NICKNAME = "D_NICKNAME";
  private static final String RIDER_FIRSTNAME = "R_FIRSTNAME";
  private static final String RIDER_LASTNAME = "R_LASTNAME";
  private static final String DRIVER_PICTURE = "DOC_URL";
  private static final String MAP_URL = "MAP_URL";
  private static final String RIDE_MAP = "RIDE_MAP";
  private static final String CAR_MODEL = "MODEL";
  private static final String CAR_MAKE = "CAR_MAKE";
  private static final Money FREE_CREDIT_CHARGED = Money.of(CurrencyUnit.USD, 5d);
  private static final Money TOTAL_FARE = Money.of(CurrencyUnit.USD, 19d);
  private static final Money STRIPE_CREDIT_CHARGE = Money.of(CurrencyUnit.USD, 4d);
  private static final boolean MAIN_RIDER = true;
  private static final String PHOTO_URL = "PHOTO_URL";
  private static final String START_ADDRESS = "START_ADDRESS";
  private static final String END_ADDRESS = "END_ADDRESS";
  @Mock
  private DocumentService documentService;
  @Mock
  private S3StorageService s3StorageService;
  @InjectMocks
  private PaymentHistoryDtoAssembler paymentHistoryDtoAssembler;

  @Test
  public void shouldMapAll() throws Exception {
    // given
    Rider rider = Rider.builder().build();
    rider.setId(RIDER_ID);
    rider.setUser(User.builder().firstname(RIDER_FIRSTNAME).lastname(RIDER_LASTNAME).build());
    rider.getUser().setPhotoUrl(PHOTO_URL);
    Driver driver = Driver.builder().build();
    driver.setUser(User.builder().firstname(DRIVER_FIRSTNAME).lastname(DRIVER_LASTNAME).nickName(DRIVER_NICKNAME).build());
    Ride ride = Ride.builder().cancelledOn(date(CANCEL_OFFSET)).completedOn(date(COMPLETED_OFFSET)).startedOn(date(STARTED_OFFSET))
      .status(RideStatus.ACTIVE).rider(rider).driverRating(DRIVER_RATING).rideMap(RIDE_MAP)
      .activeDriver(ActiveDriver.builder().selectedCar(Car.builder().model(CAR_MODEL).make(CAR_MAKE).build())
        .driver(driver).build())
      .start(new Address(START_ADDRESS, null))
      .end(new Address(END_ADDRESS, null))
      .build();
    ride.setId(RIDE_ID);
    ride.setFareDetails(FareDetails.builder().totalFare(TOTAL_FARE).build());
    RiderCard usedCard = RiderCard.builder().cardBrand(CardBrand.AMERICAN_EXPRESS).cardNumber(CARD_NUMBER).build();
    usedCard.setId(CARD_ID);
    FarePayment farePayment = FarePayment.builder().ride(ride).usedCard(usedCard)
      .freeCreditCharged(FREE_CREDIT_CHARGED).mainRider(MAIN_RIDER).stripeCreditCharge(STRIPE_CREDIT_CHARGE)
      .provider(PaymentProvider.CREDIT_CARD)
      .build();
    farePayment.setId(FARE_PAYMENT_ID);
    when(documentService.findAvatarDocument(driver, DocumentType.DRIVER_PHOTO)).thenReturn(Document.builder().documentUrl(DRIVER_PICTURE).build());
    when(s3StorageService.getSignedURL(RIDE_MAP)).thenReturn(MAP_URL);

    // when
    PaymentHistoryDto result = paymentHistoryDtoAssembler.toDto(farePayment);

    // then
    assertEquals(FARE_PAYMENT_ID, result.getFarePaymentId());
    assertEquals(RIDE_ID, result.getRideId());
    assertEquals(dateString(CANCEL_OFFSET), result.getCancelledOn());
    assertEquals(dateString(COMPLETED_OFFSET), result.getCompletedOn());
    assertEquals(dateString(STARTED_OFFSET), result.getStartedOn());
    assertEquals(DRIVER_RATING, result.getDriverRating());
    assertEquals(CARD_NUMBER, result.getCardNumber());
    assertEquals(CARD_ID, result.getUsedCardId());
    assertEquals("AMERICAN_EXPRESS", result.getUsedCardBrand());
    assertEquals(DRIVER_FIRSTNAME, result.getDriverFirstName());
    assertEquals(DRIVER_LASTNAME, result.getDriverLastName());
    assertEquals(DRIVER_NICKNAME, result.getDriverNickName());
    assertEquals(DRIVER_PICTURE, result.getDriverPicture());
    assertEquals(MAP_URL, result.getMapUrl());
    assertEquals(CAR_MAKE, result.getCarBrand());
    assertEquals(CAR_MODEL, result.getCarModel());
    assertEquals(FREE_CREDIT_CHARGED, result.getFreeCreditCharged());
    assertEquals("ACTIVE", result.getRideStatus());
    assertEquals(MAIN_RIDER, result.isMainRider());
    assertEquals(RIDER_FIRSTNAME, result.getMainRiderFistName());
    assertEquals(RIDER_LASTNAME, result.getMainRiderLastName());
    assertEquals(RIDER_ID, result.getMainRiderId());
    assertEquals(PHOTO_URL, result.getMainRiderPicture());
    assertEquals(START_ADDRESS, result.getRideStartAddress());
    assertEquals(END_ADDRESS, result.getRideEndAddress());
    assertEquals(TOTAL_FARE, result.getRideTotalFare());
    assertEquals(STRIPE_CREDIT_CHARGE, result.getStripeCreditCharge());
  }

  @Test
  public void shouldMapNullable() throws Exception {
    // given
    Rider rider = Rider.builder().build();
    rider.setId(RIDER_ID);
    rider.setUser(User.builder().firstname(RIDER_FIRSTNAME).lastname(RIDER_LASTNAME).build());
    rider.getUser().setPhotoUrl(PHOTO_URL);
    Driver driver = Driver.builder().build();
    driver.setUser(User.builder().firstname(DRIVER_FIRSTNAME).lastname(DRIVER_LASTNAME).nickName(DRIVER_NICKNAME).build());
    Ride ride = Ride.builder()
      .status(RideStatus.ACTIVE).rider(rider).driverRating(DRIVER_RATING)
      .activeDriver(ActiveDriver.builder().driver(driver).build())
      .build();
    ride.setId(RIDE_ID);
    ride.setFareDetails(FareDetails.builder().totalFare(TOTAL_FARE).build());
    FarePayment farePayment = FarePayment.builder().ride(ride).freeCreditCharged(FREE_CREDIT_CHARGED)
      .mainRider(MAIN_RIDER).stripeCreditCharge(STRIPE_CREDIT_CHARGE).provider(PaymentProvider.CREDIT_CARD).build();
    farePayment.setId(FARE_PAYMENT_ID);
    when(documentService.findAvatarDocument(driver, DocumentType.DRIVER_PHOTO)).thenReturn(Document.builder().documentUrl(DRIVER_PICTURE).build());

    // when
    PaymentHistoryDto result = paymentHistoryDtoAssembler.toDto(farePayment);

    // then
    assertEquals(FARE_PAYMENT_ID, result.getFarePaymentId());
    assertEquals(RIDE_ID, result.getRideId());
    assertEquals(null, result.getCancelledOn());
    assertEquals(null, result.getCompletedOn());
    assertEquals(null, result.getStartedOn());
    assertEquals(DRIVER_RATING, result.getDriverRating());
    assertEquals(null, result.getCardNumber());
    assertEquals(null, result.getUsedCardId());
    assertEquals(null, result.getUsedCardBrand());
    assertEquals(DRIVER_FIRSTNAME, result.getDriverFirstName());
    assertEquals(DRIVER_LASTNAME, result.getDriverLastName());
    assertEquals(DRIVER_NICKNAME, result.getDriverNickName());
    assertEquals(DRIVER_PICTURE, result.getDriverPicture());
    assertEquals(null, result.getMapUrl());
    assertEquals(null, result.getCarBrand());
    assertEquals(null, result.getCarModel());
    assertEquals(FREE_CREDIT_CHARGED, result.getFreeCreditCharged());
    assertEquals("ACTIVE", result.getRideStatus());
    assertEquals(MAIN_RIDER, result.isMainRider());
    assertEquals(RIDER_FIRSTNAME, result.getMainRiderFistName());
    assertEquals(RIDER_LASTNAME, result.getMainRiderLastName());
    assertEquals(RIDER_ID, result.getMainRiderId());
    assertEquals(PHOTO_URL, result.getMainRiderPicture());
    assertEquals(null, result.getRideStartAddress());
    assertEquals(null, result.getRideEndAddress());
    assertEquals(TOTAL_FARE, result.getRideTotalFare());
    assertEquals(STRIPE_CREDIT_CHARGE, result.getStripeCreditCharge());
    verifyZeroInteractions(s3StorageService);
  }

  @Test
  public void shouldMapNullableDriver() throws Exception {
    // given
    Rider rider = Rider.builder().build();
    rider.setId(RIDER_ID);
    rider.setUser(User.builder().firstname(RIDER_FIRSTNAME).lastname(RIDER_LASTNAME).build());
    rider.getUser().setPhotoUrl(PHOTO_URL);
    Ride ride = Ride.builder()
      .status(RideStatus.ACTIVE).rider(rider).driverRating(DRIVER_RATING)
      .build();
    ride.setId(RIDE_ID);
    ride.setFareDetails(FareDetails.builder().totalFare(TOTAL_FARE).build());
    FarePayment farePayment = FarePayment.builder().ride(ride).freeCreditCharged(FREE_CREDIT_CHARGED)
      .mainRider(MAIN_RIDER).stripeCreditCharge(STRIPE_CREDIT_CHARGE).provider(PaymentProvider.CREDIT_CARD).build();
    farePayment.setId(FARE_PAYMENT_ID);

    // when
    PaymentHistoryDto result = paymentHistoryDtoAssembler.toDto(farePayment);

    // then
    assertEquals(FARE_PAYMENT_ID, result.getFarePaymentId());
    assertEquals(RIDE_ID, result.getRideId());
    assertEquals(null, result.getCancelledOn());
    assertEquals(null, result.getCompletedOn());
    assertEquals(null, result.getStartedOn());
    assertEquals(DRIVER_RATING, result.getDriverRating());
    assertEquals(null, result.getCardNumber());
    assertEquals(null, result.getUsedCardId());
    assertEquals(null, result.getUsedCardBrand());
    assertEquals(null, result.getDriverFirstName());
    assertEquals(null, result.getDriverLastName());
    assertEquals(null, result.getDriverNickName());
    assertEquals(null, result.getDriverPicture());
    assertEquals(null, result.getMapUrl());
    assertEquals(null, result.getCarBrand());
    assertEquals(null, result.getCarModel());
    assertEquals(FREE_CREDIT_CHARGED, result.getFreeCreditCharged());
    assertEquals("ACTIVE", result.getRideStatus());
    assertEquals(MAIN_RIDER, result.isMainRider());
    assertEquals(RIDER_FIRSTNAME, result.getMainRiderFistName());
    assertEquals(RIDER_LASTNAME, result.getMainRiderLastName());
    assertEquals(RIDER_ID, result.getMainRiderId());
    assertEquals(PHOTO_URL, result.getMainRiderPicture());
    assertEquals(null, result.getRideStartAddress());
    assertEquals(null, result.getRideEndAddress());
    assertEquals(TOTAL_FARE, result.getRideTotalFare());
    assertEquals(STRIPE_CREDIT_CHARGE, result.getStripeCreditCharge());
    verifyZeroInteractions(s3StorageService);
    verifyZeroInteractions(documentService);
  }

  private String dateString(int offset) throws Exception {
    if (offset < 0 || offset > 9) {
      throw new Exception("Bad Offset");
    }
    return "03/07/2017 0" + offset + ":09 AM";
  }

  private Date date(int offset) {
    return new Date(1488866946000L + offset * 3600000);
  }

}