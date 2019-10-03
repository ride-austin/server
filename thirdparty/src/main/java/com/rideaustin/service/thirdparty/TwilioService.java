package com.rideaustin.service.thirdparty;

import java.net.URI;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.RideCall;
import com.rideaustin.model.RideCallType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Avatar;
import com.rideaustin.repo.dsl.RideCallDslRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.twilio.exception.ApiConnectionException;
import com.twilio.exception.ApiException;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;

import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TwilioService extends BaseCommunicationService {

  private static final String INVALID_PHONE_NUMBER_REGEXP = "The .* number .* is not a valid.*";
  private static final int BLACKLIST_TWILIO_ERROR_CODE = 21610;
  private static final String API_ERROR_MESSAGE = "Twilio API error occurred:";

  private TwilioRestClient twilioRestClient;
  private RideCallDslRepository rideCallDslRepository;
  private PushNotificationsFacade pushNotificationsFacade;

  private String senderNumber;
  private String callbackEndpoint;

  @Inject
  public TwilioService(TwilioRestClient twilioRestClient, Environment environment, RideCallDslRepository rideCallDslRepository,
    Configuration configuration, PushNotificationsFacade pushNotificationsFacade) {
    super(configuration);
    this.twilioRestClient = twilioRestClient;
    this.rideCallDslRepository = rideCallDslRepository;

    senderNumber = environment.getProperty("sms.twilio.sender");
    callbackEndpoint = environment.getProperty("sms.twilio.callback");
    this.pushNotificationsFacade = pushNotificationsFacade;
  }

  @Override
  public SmsStatus sendSms(@Nonnull String phoneNumber, @Nonnull String message) throws ServerError {
    try {
      MessageCreator messageCreator = new MessageCreator(new PhoneNumber(phoneNumber), new PhoneNumber(senderNumber), message);
      messageCreator.create(twilioRestClient);
    } catch (ApiException e) {
      return handleApiException(e);
    } catch (Exception e) {
      throw new ServerError(e);
    }
    return SmsStatus.OK;
  }

  @Override
  @Transactional
  public CallStatus callParticipant(Ride ride, AvatarType callerType, String callerNumber) throws ServerError {
    Avatar callee = getCallee(ride, callerType);
    String to = callee.getUser().getPhoneNumber();
    return performCall(ride, callee, callerNumber, to);
  }

  @Override
  @Transactional
  public CallStatus callParticipant(Ride ride, AvatarType callerType) throws ServerError {
    Avatar callee = getCallee(ride, callerType);
    Avatar caller = getCaller(ride, callerType);
    String from = caller.getUser().getPhoneNumber();
    String to = callee.getUser().getPhoneNumber();
    return performCall(ride, callee, from, to);
  }

  @Override
  @Transactional
  public SmsStatus smsParticipant(Ride ride, AvatarType senderType, String message) throws ServerError {
    Avatar addressee = getCallee(ride, senderType);
    Avatar sender = getCaller(ride, senderType);
    String from = sender.getUser().getPhoneNumber();
    String to = addressee.getUser().getPhoneNumber();
    if (message.isEmpty()) {
      return SmsStatus.ERROR;
    }
    try {
      if (RideStatus.ONGOING_DRIVER_STATUSES.contains(ride.getStatus())) {
        String sid = createMessage(to, message);
        rideCallDslRepository.save(new RideCall(from, to, ride.getId(), sid, RideCallType.SMS));
      } else {
        createMessage(from, ErrorMessage.NOT_ONGOING_RIDE.getContent());
      }
    } catch (ApiConnectionException | ApiException ex) {
      log.error(API_ERROR_MESSAGE, ex);
      throw new TwilioSMSException(ex);
    }
    return SmsStatus.OK;
  }

  private CallStatus performCall(Ride ride, Avatar callee, String from, String to) {
    boolean callPending = rideCallDslRepository.hasUnprocessedCalls(ride.getId());
    if (callPending) {
      return CallStatus.PENDING;
    }
    try {
      String sid = Call.creator(new PhoneNumber(to), new PhoneNumber(senderNumber), URI.create(callbackEndpoint))
        .create(twilioRestClient)
        .getSid();
      rideCallDslRepository.save(new RideCall(from, to, ride.getId(), sid, RideCallType.CALL));
    } catch (ApiConnectionException ex) {
      log.error(API_ERROR_MESSAGE, ex);
      return CallStatus.ERROR;
    } catch (ApiException ex) {
      log.error(API_ERROR_MESSAGE, ex);
      if (ex.getCode() == BLACKLIST_TWILIO_ERROR_CODE) {
        pushNotificationsFacade.pushCallBlockedNotification(ride, from, callee.getUser());
      }
      return CallStatus.ERROR;
    }
    return CallStatus.OK;
  }

  private String createMessage(String to, String message) {
    return Message.creator(new PhoneNumber(to), new PhoneNumber(senderNumber), message)
      .create(twilioRestClient)
      .getSid();
  }

  private Avatar getCaller(Ride ride, AvatarType callerType) throws ServerError {
    switch (callerType) {
      case RIDER:
        return ride.getRider();
      case DRIVER:
        return ride.getActiveDriver().getDriver();
      default:
        throw new ServerError("Unknown caller type");
    }
  }

  private Avatar getCallee(Ride ride, AvatarType callerType) throws ServerError {
    switch (callerType) {
      case RIDER:
        return ride.getActiveDriver().getDriver();
      case DRIVER:
        return ride.getRider();
      default:
        throw new ServerError("Unknown caller type");
    }
  }

  private SmsStatus handleApiException(ApiException e) throws ServerError {
    if (e.getMessage().matches(INVALID_PHONE_NUMBER_REGEXP)) {
      return SmsStatus.INVALID_PHONE_NUMBER;
    }
    throw new ServerError(e);
  }
}
