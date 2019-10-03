package com.rideaustin.service;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.events.LostAndFoundTrackEvent;
import com.rideaustin.model.City;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.lostandfound.LostItemInfo;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.LostAndFoundDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.email.LostItemEmail;
import com.rideaustin.service.thirdparty.AbstractTemplateSMS;
import com.rideaustin.service.thirdparty.CommunicationService;
import com.rideaustin.service.thirdparty.CommunicationServiceFactory;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.LostAndFoundFallbackSMS;

public class LostAndFoundServiceTest {

  @Mock
  private Environment environment;
  @Mock
  private CommunicationService communicationService;
  @Mock
  private CommunicationServiceFactory serviceFactory;
  @Mock
  private RideDslRepository rideService;
  @Mock
  private EmailService emailService;
  @Mock
  private CityService cityService;
  @Mock
  private S3StorageService s3StorageService;
  @Mock
  private DriverService driverService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private LostAndFoundDslRepository repository;
  @Mock
  private ApplicationEventPublisher publisher;

  @Mock
  private Ride ride;
  @Mock
  private City city;

  private LostAndFoundService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(serviceFactory.createCommunicationService()).thenReturn(communicationService);
    when(environment.getProperty(anyString(), eq(Integer.class), anyInt())).thenReturn(120);

    testedInstance = new LostAndFoundService(serviceFactory, emailService, cityService, s3StorageService,
      driverService, environment, currentUserService, rideService, repository, publisher);
  }

  @Test
  public void initiateCall() throws Exception {
    LostItemFixture lostItemFixture = new LostItemFixture(CommunicationService.CallStatus.OK).invoke();
    long rideId = lostItemFixture.getRideId();
    String phoneNumber = lostItemFixture.getPhoneNumber();

    testedInstance.initiateCall(rideId, phoneNumber);

    verify(emailService, times(1)).sendEmail(any(LostItemEmail.class));
    verify(communicationService, never()).sendSms(eq("+1123DRIVER"), endsWith(phoneNumber));
    verify(publisher, times(1)).publishEvent(any(LostAndFoundTrackEvent.class));
  }

  @Test
  public void initiateCallWithFallback() throws Exception {
    LostItemFixture lostItemFixture = new LostItemFixture(CommunicationService.CallStatus.ERROR).invoke();
    long rideId = lostItemFixture.getRideId();
    String phoneNumber = lostItemFixture.getPhoneNumber();

    testedInstance.initiateCall(rideId, phoneNumber);

    verify(emailService, times(1)).sendEmail(any(LostItemEmail.class));
    verify(communicationService).sendSms(argThat(new TypeSafeMatcher<AbstractTemplateSMS>() {
      @Override
      protected boolean matchesSafely(AbstractTemplateSMS sms) {
        boolean typeMatches = sms instanceof LostAndFoundFallbackSMS;
        boolean contentMatches = false;
        if (typeMatches) {
          LostAndFoundFallbackSMS fallbackSMS = (LostAndFoundFallbackSMS) sms;
          contentMatches = fallbackSMS.getModel().get("riderPhoneNumber").equals(phoneNumber);
        }
        return typeMatches && contentMatches;
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
    verify(publisher, times(1)).publishEvent(any(LostAndFoundTrackEvent.class));
  }

  @Test
  public void processLostItem() throws Exception {
    LostItemFixture lostItemFixture = new LostItemFixture(CommunicationService.CallStatus.OK).invoke();
    long rideId = lostItemFixture.getRideId();
    String phoneNumber = lostItemFixture.getPhoneNumber();
    LostItemInfo lostItemInfo = lostItemFixture.lostItemInfo;
    ArgumentCaptor<LostItemEmail> emailCaptor = ArgumentCaptor.forClass(LostItemEmail.class);
    String description = "descr";
    String details = "details";

    testedInstance.processLostItem(rideId, description, details, phoneNumber);

    verify(emailService, times(1)).sendEmail(emailCaptor.capture());

    Map<String, Object> expectedModel = ImmutableMap.<String, Object>builder()
      .put("description", description)
      .put("details", details)
      .put("rideId", lostItemInfo.getRideId())
      .put("driverEmail", lostItemInfo.getDriverEmail())
      .put("driverName", lostItemInfo.getDriverName())
      .put("riderId", lostItemInfo.getRiderId())
      .put("riderEmail", lostItemInfo.getRiderEmail())
      .put("riderName", lostItemInfo.getRiderName())
      .put("riderPhone", phoneNumber)
      .put("city", city)
      .build();
    assertThat( expectedModel.entrySet(), everyItem(isIn(emailCaptor.getValue().getModel().entrySet())));
    assertThat( emailCaptor.getValue().getModel().entrySet(), everyItem(isIn(expectedModel.entrySet())));
    verify(publisher, times(1)).publishEvent(any(LostAndFoundTrackEvent.class));
  }

  private class LostItemFixture {
    private long rideId;
    private String phoneNumber;
    private LostItemInfo lostItemInfo;

    private CommunicationService.CallStatus callStatus;

    public LostItemFixture(CommunicationService.CallStatus callStatus) {
      this.callStatus = callStatus;
      rideId = 1L;
      phoneNumber = "+11234567890";
      lostItemInfo = LostItemInfo.builder()
        .rideId(rideId)
        .driverEmail("driver@d.com")
        .driverFirstName("DriverF")
        .driverLastName("DriverL")
        .riderUserId(1L)
        .riderId(1L)
        .riderEmail("rider@r.com")
        .riderFirstName("RiderF")
        .riderLastName("RiderL")
        .driverPhone("+1123DRIVER")
        .cityId(1L)
        .build();
    }

    public long getRideId() {
      return rideId;
    }

    public String getPhoneNumber() {
      return phoneNumber;
    }

    public LostItemInfo getLostItemInfo() {
      return lostItemInfo;
    }

    public LostItemFixture invoke() throws NotFoundException, ServerError {
      User user = new User();
      user.setId(1L);
      when(rideService.findOne(anyLong())).thenReturn(ride);
      when(communicationService.callParticipant(eq(ride), eq(AvatarType.RIDER), eq(phoneNumber))).thenReturn(callStatus);
      when(repository.getLostItemInfo(eq(rideId))).thenReturn(lostItemInfo);
      when(currentUserService.getUser()).thenReturn(user);
      when(cityService.getById(anyLong())).thenReturn(city);
      return this;
    }
  }
}