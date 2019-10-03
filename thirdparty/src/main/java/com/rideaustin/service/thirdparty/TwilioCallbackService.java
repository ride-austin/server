package com.rideaustin.service.thirdparty;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.RideCall;
import com.rideaustin.model.RideCallType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.thirdparty.CallbackResponse;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideCallDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.model.AssociatedRide;
import com.twilio.twiml.Body;
import com.twilio.twiml.Dial;
import com.twilio.twiml.Message;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.Number;
import com.twilio.twiml.Pause;
import com.twilio.twiml.Reject;
import com.twilio.twiml.Say;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class TwilioCallbackService implements CommunicationCallbackService {


  private static final String VOICE_MESSAGE = "This is a call from Ride Austin. Please wait while we connect you to a rider who believes they left something in your vehicle.";

  private final RideCallDslRepository rideCallDslRepository;
  private final RideDslRepository rideDslRepository;
  private final UserDslRepository userDslRepository;

  private final String anonymousNumber;
  private final int dialTimeout;
  private final int voiceDialTimeout;

  @Inject
  public TwilioCallbackService(RideCallDslRepository rideCallDslRepository, RideDslRepository rideDslRepository,
    Environment environment, UserDslRepository userDslRepository) {
    this.rideCallDslRepository = rideCallDslRepository;
    this.rideDslRepository = rideDslRepository;
    this.userDslRepository = userDslRepository;

    this.anonymousNumber = environment.getProperty("sms.twilio.sender");
    this.dialTimeout = environment.getProperty("twilio.dial.timeout", Integer.class, 15);
    this.voiceDialTimeout = environment.getProperty("twilio.dial.voice.timeout", Integer.class, 30);
  }

  @Override
  public CallbackResponse createCallbackResponse(String callSid, String from) throws ServerError {
    try {
      CallbackResponse response;
      RideCall existing = rideCallDslRepository.findBySid(callSid);
      if (existing == null) {
        AssociatedRide associatedRide = resolveRide(from);
        if (associatedRide == null) {
          response = createRejectResponse();
        } else {
          response = processDirectCall(callSid, from, associatedRide.id, resolveReceiver(from, associatedRide));
        }
      } else {
        response = processCallCallback(existing);
      }
      return response;
    } catch (TwiMLException e) {
      throw new ServerError(e);
    }
  }

  @Override
  public CallbackResponse createSmsCallbackResponse(String messageSid, String sender, String body) throws ServerError {
    List<User> candidateSender = userDslRepository.findByPhoneNumber(sender);
    AssociatedRide associatedRide = resolveRide(sender);
    try {
      CallbackResponse response;
      if (candidateSender.isEmpty()) {
        log.warn(String.format("Message from %s won't be delivered as this number is not associated with any user", sender));
        response = createMessageNonDeliveredResponse(sender, CommunicationService.ErrorMessage.UNKNOWN_NUMBER);
      } else if (associatedRide == null) {
        log.warn(String.format("Message from %s won't be delivered as no associated ride is found", sender));
        response = createMessageNonDeliveredResponse(sender, CommunicationService.ErrorMessage.NOT_ONGOING_RIDE);
      } else {
        log.warn(String.format("Message from %s is associated with ride #%d and will be processed as initiative", sender, associatedRide.id));
        String receiver = resolveReceiver(sender, associatedRide);
        rideCallDslRepository.save(new RideCall(sender, receiver, associatedRide.id, messageSid, RideCallType.SMS));
        return createMessageResponse(body, receiver);
      }
      return response;
    } catch (TwiMLException e) {
      throw new ServerError(e);
    }
  }

  private AssociatedRide resolveRide(String from) {
    List<AssociatedRide> rides = rideDslRepository.findOngoingRideByParticipantPhoneNumber(from);
    if (rides.size() == 1) {
      return rides.get(0);
    } else if (rides.size() == 2) {
      Map<RideStatus, AssociatedRide> mapped = rides.stream().collect(Collectors.toMap(AssociatedRide::getRideStatus, Function.identity()));
      return mapped.get(RideStatus.DRIVER_ASSIGNED);
    } else {
      return null;
    }
  }

  private CallbackResponse processDirectCall(String callSid, String from, Long rideId, String receiver) throws TwiMLException {
    rideCallDslRepository.save(new RideCall(from, receiver, rideId, callSid, RideCallType.CALL));
    return createDialResponse(receiver, this.anonymousNumber);
  }

  private CallbackResponse processCallCallback(RideCall rideCall) throws TwiMLException {
    CallbackResponse response = createVoiceDialResponse(rideCall.getFrom());
    rideCall.setProcessed(true);
    rideCallDslRepository.save(rideCall);
    return response;
  }

  private CallbackResponse createMessageNonDeliveredResponse(String from, CommunicationService.ErrorMessage message) throws TwiMLException {
    return createMessageResponse(String.format(message.getContent(), from), from);
  }

  private CallbackResponse createMessageResponse(String body, String to) throws TwiMLException {
    return new CallbackResponse(
      new MessagingResponse.Builder()
        .message(
          new Message.Builder()
            .body(
              new Body(body)
            )
            .to(to)
            .build()
        )
        .build()
        .toXml()
    );
  }

  private CallbackResponse createRejectResponse() throws TwiMLException {
    return new CallbackResponse(
      new VoiceResponse.Builder()
        .reject(
          new Reject.Builder().reason(Reject.Reason.REJECTED).build()
        )
        .build()
        .toXml()
    );
  }

  private CallbackResponse createVoiceDialResponse(String receiver) throws TwiMLException {
    Dial.Builder dialBuilder = new Dial.Builder()
      .number(
        new Number.Builder(receiver).build()
      )
      .timeout(voiceDialTimeout);
    Say.Builder sayBuilder = new Say.Builder(VOICE_MESSAGE)
      .language(Say.Language.EN)
      .loop(1)
      .voice(Say.Voice.ALICE);
    return new CallbackResponse(
      new VoiceResponse.Builder()
        .pause(new Pause.Builder().length(2).build())
        .say(sayBuilder.build())
        .dial(dialBuilder.build())
        .build()
        .toXml()
    );
  }

  private CallbackResponse createDialResponse(String receiver, String caller) throws TwiMLException {
    Dial.Builder dialBuilder = new Dial.Builder()
      .number(
        new Number.Builder(receiver).build()
      )
      .timeout(dialTimeout);
    if (caller != null) {
      dialBuilder.callerId(caller);
    }
    return new CallbackResponse(
      new VoiceResponse.Builder()
        .dial(dialBuilder.build())
        .build()
        .toXml()
    );
  }

  private String resolveReceiver(String sender, AssociatedRide associatedRide) {
    return associatedRide.driverPhone.equals(sender) ? associatedRide.riderPhone : associatedRide.driverPhone;
  }

}
