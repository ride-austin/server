package com.rideaustin.service.thirdparty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableList;
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
import com.rideaustin.utils.RandomString;

public class TwilioCallbackServiceTest {

  private static final String MESSAGE = "MESSAGE";
  private static final String ERROR_RESPONSE = "<Response><Message to=\"FROM\"><Body>Sorry we can't deliver your message because your ride has been finished or cancelled.</Body></Message></Response>";
  private static final String NO_USER_ERROR_RESPONSE = "<Response><Message to=\"FROM\"><Body>Sorry, we can't deliver your message as phone number FROM is not associated with your user profile. Please check your phone number in application settings</Body></Message></Response>";
  private static final String FROM = "FROM";
  private static final Long RIDE_ID = 1L;
  private static final String ANONYMOUS = "TWILIO";
  private TwilioCallbackService testedInstance;
  @Mock
  private RideCallDslRepository rideCallDslRepository;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private Environment environment;
  @Mock
  private UserDslRepository userDslRepository;
  @Captor
  private ArgumentCaptor<RideCall> captor;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(environment.getProperty("sms.twilio.sender")).thenReturn(ANONYMOUS);
    when(environment.getProperty("twilio.dial.timeout", Integer.class, 15)).thenReturn(15);
    when(environment.getProperty("twilio.dial.voice.timeout", Integer.class, 30)).thenReturn(30);

    testedInstance = new TwilioCallbackService(rideCallDslRepository, rideDslRepository, environment, userDslRepository);
  }

  @Test
  public void testCreateCallbackResponseRejectsCallIfNoRideIsAssociatedWithCallerNumber() throws ServerError {
    when(rideDslRepository.findOngoingRideByParticipantPhoneNumber(eq(FROM))).thenReturn(Collections.emptyList());

    CallbackResponse callbackResponse = testedInstance.createCallbackResponse(RandomString.generate(), FROM);

    assertEquals("<Response><Reject reason=\"rejected\"/></Response>", callbackResponse.getResponse());
  }

  @Test
  public void testCreateCallbackResponseSearchesForExistingConversationIfAssociatedRideIsFound() throws ServerError {
    AssociatedRide ride = createRide(FROM, "TO");
    when(rideDslRepository.findOngoingRideByParticipantPhoneNumber(eq(FROM))).thenReturn(ImmutableList.of(ride));

    String callSid = RandomString.generate();
    testedInstance.createCallbackResponse(callSid, FROM);

    verify(rideCallDslRepository, times(1)).findBySid(eq(callSid));
  }

  @Test
  public void testCreateCallbackResponseInitiatesConversationFromDriverToRider() throws ServerError {
    AssociatedRide ride = createRide("DRIVER", "RIDER");
    when(rideDslRepository.findOngoingRideByParticipantPhoneNumber(eq("DRIVER"))).thenReturn(ImmutableList.of(ride));

    String callSid = RandomString.generate();
    CallbackResponse response = testedInstance.createCallbackResponse(callSid, "DRIVER");

    assertEquals("<Response><Dial timeout=\"15\" callerId=\"TWILIO\"><Number>RIDER</Number></Dial></Response>", response.getResponse());
    verify(rideCallDslRepository, times(1)).save(captor.capture());
    assertCall(callSid, "DRIVER", "RIDER", captor.getValue());
  }

  @Test
  public void testCreateCallbackInitiatesConversationFromRiderToDriver() throws ServerError {
    AssociatedRide ride = createRide("RIDER", "DRIVER");
    when(rideDslRepository.findOngoingRideByParticipantPhoneNumber(eq("RIDER"))).thenReturn(ImmutableList.of(ride));

    String callSid = RandomString.generate();
    CallbackResponse response = testedInstance.createCallbackResponse(callSid, "RIDER");

    assertEquals("<Response><Dial timeout=\"15\" callerId=\"TWILIO\"><Number>DRIVER</Number></Dial></Response>", response.getResponse());
    verify(rideCallDslRepository, times(1)).save(captor.capture());
    assertCall(callSid, "RIDER", "DRIVER", captor.getValue());
  }

  @Test
  public void testCreateCallbackProcessesReplyIfExistingConversationIsFound() throws ServerError {
    AssociatedRide ride = createRide("DRIVER", "RIDER");
    String callSid = RandomString.generate();
    when(rideDslRepository.findOngoingRideByParticipantPhoneNumber(eq("RIDER"))).thenReturn(ImmutableList.of(ride));
    when(rideCallDslRepository.findBySid(eq(callSid))).thenReturn(createRideSMS(callSid, "DRIVER", "RIDER"));

    CallbackResponse response = testedInstance.createCallbackResponse(callSid, "RIDER");
    assertEquals("<Response><Pause length=\"2\"/><Say loop=\"1\" language=\"en\" voice=\"alice\">This is a call from Ride Austin. Please wait while we connect you to a rider who believes they left something in your vehicle.</Say><Dial timeout=\"30\"><Number>DRIVER</Number></Dial></Response>", response.getResponse());
    verify(rideCallDslRepository, times(1)).save(captor.capture());

    assertTrue(captor.getAllValues().get(0).isProcessed());
  }


  @Test
  public void testCreateSmsCallbackReturnsFallbackMessageWhenNoRideIsAssociatedWithSenderNumber() throws ServerError {
    when(rideDslRepository.findOngoingRideByParticipantPhoneNumber(eq(FROM))).thenReturn(Collections.emptyList());
    when(userDslRepository.findByPhoneNumber(anyString())).thenReturn(ImmutableList.of(new User()));

    CallbackResponse response = testedInstance.createSmsCallbackResponse(RandomString.generate(), FROM, MESSAGE);
    assertEquals(ERROR_RESPONSE, response.getResponse());
  }

  @Test
  public void testCreateSmsCallbackInitiatesConversationFromDriverToRider() throws ServerError {
    AssociatedRide ride = createRide("DRIVER", "RIDER");
    when(rideDslRepository.findOngoingRideByParticipantPhoneNumber(eq("DRIVER"))).thenReturn(ImmutableList.of(ride));
    when(userDslRepository.findByPhoneNumber(anyString())).thenReturn(ImmutableList.of(new User()));

    String messageSid = RandomString.generate();
    CallbackResponse response = testedInstance.createSmsCallbackResponse(messageSid, "DRIVER", MESSAGE);

    assertEquals("<Response><Message to=\"RIDER\"><Body>MESSAGE</Body></Message></Response>", response.getResponse());
    verify(rideCallDslRepository, times(1)).save(captor.capture());
    assertSMS(messageSid, "DRIVER", "RIDER", captor.getValue());
  }

  @Test
  public void testCreateSmsCallbackInitiatesConversationFromRiderToDriver() throws ServerError {
    AssociatedRide ride = createRide("DRIVER", "RIDER");
    when(rideDslRepository.findOngoingRideByParticipantPhoneNumber(eq("RIDER"))).thenReturn(ImmutableList.of(ride));
    when(userDslRepository.findByPhoneNumber(anyString())).thenReturn(ImmutableList.of(new User()));

    String messageSid = RandomString.generate();
    CallbackResponse response = testedInstance.createSmsCallbackResponse(messageSid, "RIDER", MESSAGE);

    assertEquals("<Response><Message to=\"DRIVER\"><Body>MESSAGE</Body></Message></Response>", response.getResponse());
    verify(rideCallDslRepository, times(1)).save(captor.capture());
    assertSMS(messageSid, "RIDER", "DRIVER", captor.getValue());
  }

  @Test
  public void testCreateSmsCallbackReturnsErrorMessageIfNoUserWithSenderPhoneNumberIsFound() throws ServerError {
    when(rideDslRepository.findOngoingRideByParticipantPhoneNumber(eq(FROM))).thenReturn(Collections.emptyList());
    when(userDslRepository.findByPhoneNumber(anyString())).thenReturn(Collections.emptyList());

    CallbackResponse response = testedInstance.createSmsCallbackResponse(RandomString.generate(), FROM, MESSAGE);
    assertEquals(NO_USER_ERROR_RESPONSE, response.getResponse());
  }

  private RideCall createRideSMS(String sid, String sender, String receiver) {
    return new RideCall(sender, receiver, RIDE_ID, sid, RideCallType.SMS);
  }

  private AssociatedRide createRide(String driverPhoneNumber, String riderPhoneNumber) {
    return new AssociatedRide(RIDE_ID, RideStatus.DRIVER_ASSIGNED, riderPhoneNumber, driverPhoneNumber);
  }

  private void assertSMS(String messageSid, String sender, String receiver, RideCall actual) {
    assertEquals(sender, actual.getFrom());
    assertEquals(receiver, actual.getTo());
    assertEquals(RIDE_ID, actual.getRideId());
    assertEquals(messageSid, actual.getCallSid());
    assertEquals(RideCallType.SMS, actual.getType());
  }

  private void assertCall(String callSid, String sender, String receiver, RideCall actual) {
    assertEquals(sender, actual.getFrom());
    assertEquals(receiver, actual.getTo());
    assertEquals(RIDE_ID, actual.getRideId());
    assertEquals(callSid, actual.getCallSid());
    assertEquals(RideCallType.CALL, actual.getType());
  }

}