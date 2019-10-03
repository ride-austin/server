package com.rideaustin.service.thirdparty;

import java.util.List;

import javax.annotation.Nonnull;

import org.joda.money.Money;
import org.springframework.stereotype.Service;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.rest.exception.RideAustinException;

@Service
public interface StripeService {

  String createStripeAccount(Rider rider) throws RideAustinException;

  RiderCard createCardForRider(Rider rider, String cardToken) throws RideAustinException;

  List<RiderCard> listRiderCards(Rider rider, List<RiderCard> riderCardsInDB) throws RideAustinException;

  void deleteCardForRider(RiderCard riderCard) throws RideAustinException;

  String receiveTokenPayment(Rider rider, Ride ride, Money chargeAmount) throws RideAustinException;

  String holdTokenPayment(Rider rider, Ride ride, Money chargeAmount) throws RideAustinException;

  String receiveCardPayment(Rider rider, Ride ride, Rider cardRider, String stripeCardToken, Money chargeAmount) throws RideAustinException;

  String holdCardPayment(Rider rider, Ride ride, Rider cardRider, String stripeCardToken, Money chargeAmount) throws RideAustinException;

  void updateCardFingerPrint(RiderCard riderCard) throws RideAustinException;

  void updateCardExpiration(RiderCard riderCard) throws RideAustinException;

  String authorizeRide(Ride ride, RiderCard card) throws RideAustinException;

  String authorizeRide(Ride ride, String token) throws RideAustinException;

  void refundPreCharge(@Nonnull Ride ride) throws RideAustinException;

  String captureCharge(Ride ride, Money override) throws RideAustinException;

  void refundCharge(String chargeId) throws RideAustinException;
}
