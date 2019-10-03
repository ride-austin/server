package com.rideaustin.events.listeners;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.events.RideAcceptedEvent;
import com.rideaustin.model.MessagingRideInfoDTO;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.RideService;
import com.rideaustin.service.thirdparty.CommunicationServiceFactory;
import com.rideaustin.service.user.RideAcceptedSMS;

@Component
public class RideAcceptedMessagingEventListener extends BaseMessagingEventListener<RideAcceptedSMS> {

  private final RideService rideService;

  @Inject
  public RideAcceptedMessagingEventListener(CommunicationServiceFactory communicationServiceFactory, RideService rideService,
    RideDslRepository repository, ConfigurationItemCache configurationCache) {
    super(communicationServiceFactory, repository, configurationCache);
    this.rideService = rideService;
  }

  @EventListener
  public void handle(RideAcceptedEvent event) {
    super.handle(event);
  }

  @Override
  protected RideAcceptedSMS getMessage(MessagingRideInfoDTO info) {
    return new RideAcceptedSMS(info);
  }

  protected void enrichMessageInfo(long rideId, MessagingRideInfoDTO info) {
    long eta = rideService.getDrivingTimeToRider(rideId);
    info.setDrivingTimeToRider(eta);
  }
}
