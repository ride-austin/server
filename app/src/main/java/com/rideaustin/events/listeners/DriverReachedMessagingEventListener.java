package com.rideaustin.events.listeners;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.events.DriverReachedEvent;
import com.rideaustin.model.MessagingRideInfoDTO;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.thirdparty.CommunicationServiceFactory;
import com.rideaustin.service.user.DriverReachedSMS;

@Component
public class DriverReachedMessagingEventListener extends BaseMessagingEventListener<DriverReachedSMS> {

  @Inject
  public DriverReachedMessagingEventListener(CommunicationServiceFactory communicationServiceFactory, RideDslRepository repository, ConfigurationItemCache configurationCache) {
    super(communicationServiceFactory, repository, configurationCache);
  }

  @EventListener
  public void handle(DriverReachedEvent driverReachedEvent) {
    super.handle(driverReachedEvent);
  }

  @Override
  protected DriverReachedSMS getMessage(MessagingRideInfoDTO info) {
    return new DriverReachedSMS(info);
  }

  @Override
  protected void enrichMessageInfo(long rideId, MessagingRideInfoDTO info) {
    //do nothing
  }

}
