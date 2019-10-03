package com.rideaustin.service.thirdparty;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.RideCall;
import com.rideaustin.model.RideCallType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideCallDslRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.rideaustin.utils.RandomString;
import com.twilio.exception.ApiException;
import com.twilio.http.Response;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;

import freemarker.template.Configuration;

public class TwilioServiceTest {

  private static final String DRIVER_PHONE_NUMBER = "+1DRIVER";
  private static final String RIDER_PHONE_NUMBER = "+1RIDER";
  private static final String MESSAGE = "Hello";

  private TwilioService testedInstance;
  @Mock
  private TwilioRestClient twilioRestClient;
  @Mock
  private RideCallDslRepository rideCallDslRepository;
  @Mock
  private Environment environment;
  @Mock
  private Configuration configuration;
  @Mock
  private PushNotificationsFacade pushNotificationsFacade;
  @Captor
  private ArgumentCaptor<RideCall> rideCall;
  @Mock
  private Response response;
  @Mock
  private ObjectMapper mapper;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(environment.getProperty(eq("sms.twilio.sender"))).thenReturn(RandomString.generate(10));
    when(environment.getProperty(eq("sms.twilio.callback"))).thenReturn(RandomString.generate(10));

    testedInstance = new TwilioService(twilioRestClient, environment, rideCallDslRepository, configuration, pushNotificationsFacade);
  }

  @Test(expected = ServerError.class)
  public void callParticipantThrowsServerErrorOnInvalidAvatarType() throws Exception {
    Ride ride = createRide();

    testedInstance.callParticipant(ride, AvatarType.ADMIN);
  }

  @Test
  public void callParticipantCreatesCallInitiatedByRider() throws ServerError, IOException {
    Ride ride = createRide();
    String callSid = RandomString.generate(32);
    prepareSuccessfulCall(callSid);

    CommunicationService.CallStatus callStatus = testedInstance.callParticipant(ride, AvatarType.RIDER);

    assertEquals(CommunicationService.CallStatus.OK, callStatus);
    verify(rideCallDslRepository, times(1)).save(rideCall.capture());
    assertCall(callSid, RIDER_PHONE_NUMBER, DRIVER_PHONE_NUMBER);
  }

  @Test
  public void callParticipantCreatesCallInitiatedByDriver() throws ServerError, IOException {
    Ride ride = createRide();
    String callSid = RandomString.generate(32);
    prepareSuccessfulCall(callSid);

    CommunicationService.CallStatus callStatus = testedInstance.callParticipant(ride, AvatarType.DRIVER);

    assertEquals(CommunicationService.CallStatus.OK, callStatus);
    verify(rideCallDslRepository, times(1)).save(rideCall.capture());
    assertCall(callSid, DRIVER_PHONE_NUMBER, RIDER_PHONE_NUMBER);
  }

  @Test
  public void callParticipantReturnsErrorOnTwilioError() throws ServerError {
    Ride ride = createRide();

    CommunicationService.CallStatus callStatus = testedInstance.callParticipant(ride, AvatarType.RIDER);

    assertEquals(CommunicationService.CallStatus.ERROR, callStatus);
  }

  @Test
  public void callParticipantSendsPushNotificationIfBlacklistErrorOccurs() throws Exception {
    Ride ride = createRide();
    when(twilioRestClient.request(any())).thenThrow(new ApiException("", 21610, "", 500, null));

    testedInstance.callParticipant(ride, AvatarType.RIDER);

    verify(pushNotificationsFacade, only()).pushCallBlockedNotification(eq(ride), eq(RIDER_PHONE_NUMBER), eq(ride.getActiveDriver().getDriver().getUser()));
  }

  @Test(expected = ServerError.class)
  public void smsParticipantThrowsServerErrorOnInvalidAvatarType() throws Exception {
    Ride ride = createRide();

    testedInstance.smsParticipant(ride, AvatarType.ADMIN, MESSAGE);
  }

  @Test
  public void smsParticipantCreatesMessageInitiatedByRider() throws ServerError, IOException {
    Ride ride = createRide();
    String messageSid = RandomString.generate(32);
    prepareSuccessfulMessage(messageSid);

    CommunicationService.SmsStatus smsStatus = testedInstance.smsParticipant(ride, AvatarType.RIDER, MESSAGE);

    assertEquals(CommunicationService.SmsStatus.OK, smsStatus);
    verify(rideCallDslRepository, only()).save(rideCall.capture());
    assertSms(messageSid, RIDER_PHONE_NUMBER, DRIVER_PHONE_NUMBER);
  }

  @Test
  public void smsParticipantCreatesCallInitiatedByDriver() throws ServerError, IOException {
    Ride ride = createRide();
    String messageSid = RandomString.generate(32);
    prepareSuccessfulMessage(messageSid);

    CommunicationService.SmsStatus smsStatus = testedInstance.smsParticipant(ride, AvatarType.DRIVER, MESSAGE);

    assertEquals(CommunicationService.SmsStatus.OK, smsStatus);
    verify(rideCallDslRepository, only()).save(rideCall.capture());
    assertSms(messageSid, DRIVER_PHONE_NUMBER, RIDER_PHONE_NUMBER);
  }

  @Test(expected = TwilioSMSException.class)
  public void smsParticipantReturnsErrorOnTwilioError() throws ServerError {
    Ride ride = createRide();

    CommunicationService.SmsStatus smsStatus = testedInstance.smsParticipant(ride, AvatarType.RIDER, MESSAGE);

    assertEquals(CommunicationService.SmsStatus.ERROR, smsStatus);
  }

  private void prepareSuccessfulMessage(String messageSid) throws IOException {
    Message message = Message.fromJson("{\"sid\":\"" + messageSid + "\"}", new ObjectMapper());

    when(twilioRestClient.getObjectMapper()).thenReturn(mapper);
    when(twilioRestClient.request(any())).thenReturn(response);
    when(response.getStatusCode()).thenReturn(200);
    when(mapper.readValue(any(InputStream.class), eq(Message.class))).thenReturn(message);
  }

  private void prepareSuccessfulCall(String callSid) throws IOException {
    Call call = Call.fromJson("{\"sid\":\"" + callSid + "\"}", new ObjectMapper());

    when(twilioRestClient.getObjectMapper()).thenReturn(mapper);
    when(twilioRestClient.request(any())).thenReturn(response);
    when(response.getStatusCode()).thenReturn(200);
    when(mapper.readValue(any(InputStream.class), eq(Call.class))).thenReturn(call);
  }

  private void assertSms(String sid, String from, String to) {
    assertRideCall(sid, from, to, RideCallType.SMS);
  }

  private void assertCall(String sid, String from, String to) {
    assertRideCall(sid, from, to, RideCallType.CALL);
  }

  private void assertRideCall(String sid, String from, String to, RideCallType type) {
    assertEquals(from, rideCall.getValue().getFrom());
    assertEquals(to, rideCall.getValue().getTo());
    assertEquals(sid, rideCall.getValue().getCallSid());
    assertEquals(type, rideCall.getValue().getType());
  }

  private Ride createRide() {
    Ride ride = new Ride();
    ride.setRider(createRider());
    ride.setActiveDriver(createActiveDriver());
    ride.setStatus(RideStatus.ACTIVE);
    return ride;
  }

  private ActiveDriver createActiveDriver() {
    ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setDriver(createDriver());
    return activeDriver;
  }

  private Driver createDriver() {
    Driver driver = new Driver();
    driver.setUser(createUser(DRIVER_PHONE_NUMBER));
    return driver;
  }

  private Rider createRider() {
    Rider rider = new Rider();
    rider.setUser(createUser(RIDER_PHONE_NUMBER));
    return rider;
  }

  private User createUser(String phoneNumber) {
    User user = new User();
    user.setPhoneNumber(phoneNumber);
    return user;
  }

}