package com.rideaustin.test.stubs.transactional;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.CampaignService;
import com.rideaustin.service.CurrentSessionService;
import com.rideaustin.service.FareService;
import com.rideaustin.service.RiderCardService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.farepayment.FarePaymentService;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.rideaustin.service.payment.PaymentEmailService;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.ride.RideLoadService;
import com.rideaustin.service.thirdparty.StripeService;

public class PaymentService extends com.rideaustin.service.payment.PaymentService {

  public PaymentService(CurrentSessionService currentSessionService, StripeService stripeService,
    RideDslRepository rideDslRepository, PaymentEmailService paymentEmailService,
    RiderDslRepository riderDslRepository, PromocodeService promocodeService,
    FarePaymentService farePaymentService, FareService fareService, RiderCardService riderCardService,
    RideLoadService rideLoadService, PushNotificationsFacade pushNotificationsFacade, CampaignService campaignService,
    RidePaymentConfig config) {
    super(currentSessionService, stripeService, rideDslRepository, paymentEmailService, riderDslRepository,
      promocodeService, farePaymentService, fareService, riderCardService, rideLoadService,
      pushNotificationsFacade, campaignService, config);
  }

  @Override
  public boolean processRidePayment(Ride ride) throws RideAustinException {
    return processRidePayment(ride.getId());
  }
}
