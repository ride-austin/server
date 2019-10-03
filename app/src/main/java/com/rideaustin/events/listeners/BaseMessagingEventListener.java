package com.rideaustin.events.listeners;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.events.MessagingEvent;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.MessagingRideInfoDTO;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.thirdparty.AbstractTemplateSMS;
import com.rideaustin.service.thirdparty.CommunicationService;
import com.rideaustin.service.thirdparty.CommunicationServiceFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public abstract class BaseMessagingEventListener<T extends AbstractTemplateSMS> {

  private final CommunicationService communicationService;
  private final RideDslRepository repository;
  private final ConfigurationItemCache configurationCache;

  @Inject
  public BaseMessagingEventListener(CommunicationServiceFactory communicationServiceFactory, RideDslRepository repository,
    ConfigurationItemCache configurationCache) {
    this.communicationService = communicationServiceFactory.createCommunicationService();
    this.repository = repository;
    this.configurationCache = configurationCache;
  }

  protected void handle(MessagingEvent event) {
    long rideId = event.getRideId();
    try {
      MessagingRideInfoDTO info = repository.getMessagingRideInfo(rideId);
      if (info != null && configurationCache.getConfigAsBoolean(ClientType.CONSOLE, "rideMessaging", "enabled", info.getCityId())) {
        enrichMessageInfo(rideId, info);
        communicationService.sendSms(getMessage(info));
      }
    } catch (Exception e) {
      log.error("Failed to send text message", e);
    }
  }

  protected abstract void enrichMessageInfo(long rideId, MessagingRideInfoDTO info);

  protected abstract T getMessage(MessagingRideInfoDTO info);

}
