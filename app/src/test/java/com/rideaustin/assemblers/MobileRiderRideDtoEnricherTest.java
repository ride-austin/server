package com.rideaustin.assemblers;

import static com.rideaustin.test.util.TestUtils.money;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;

import com.google.common.collect.ImmutableMap;
import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.StubStateMachineContext;
import com.rideaustin.model.Address;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.RideUpgradeRequest;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.CityCarTypeDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.MobileRiderRideDto.ActiveDriverDto;
import com.rideaustin.rest.model.MobileRiderRideDto.PrecedingRide;
import com.rideaustin.service.CarTypeService;
import com.rideaustin.service.MapService;
import com.rideaustin.service.UpdateDistanceTimeService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.ride.RideUpgradeService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;

public class MobileRiderRideDtoEnricherTest {

  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private Environment environment;
  @Mock
  private RideUpgradeService upgradeService;
  @Mock
  private ObjectLocationService<OnlineDriverDto> objectLocationService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RidePaymentConfig paymentConfig;
  @Mock
  private DocumentDslRepository documentDslRepository;
  @Mock
  private CarDocumentDslRepository carDocumentDslRepository;
  @Mock
  private MapService mapService;
  @Mock
  private UpdateDistanceTimeService updateDistanceTimeService;
  @Mock
  private CityCarTypeDtoAssembler cityCarTypeDtoAssembler;
  @Mock
  private CarTypeService carTypeService;
  @Mock
  private CarTypesCache carTypesCache;

  private MobileRiderRideDtoEnricher<MobileRiderRideDto> testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    CarTypesUtils.setCarTypesCache(carTypesCache);

    testedInstance = new MobileRiderRideDtoEnricher<>(contextAccess, environment, upgradeService, objectLocationService, rideDslRepository,
      paymentConfig, documentDslRepository, carDocumentDslRepository, mapService, cityCarTypeDtoAssembler, updateDistanceTimeService,
      carTypeService);
  }

  @Test
  public void enrichSkipsNull() {
    final MobileRiderRideDto result = testedInstance.enrich(null);

    assertNull(result);
  }

  @Test
  public void enrichSetsActiveDriverFromDatabase() throws Exception {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.ACTIVE, null, null, null, 5.0, null, 34.98191,
      -97.498191, null, null, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, null, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(1L);
    dispatchContext.setCandidate(candidate);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "dispatchContext", dispatchContext
    )));
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(candidate.getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    final ActiveDriverDto activeDriverDto = new ActiveDriverDto(1L, 1L, 5.0, null,
      1L, "email@email.com", "A", "B", "C", "+15555555555",
      true, 1L, "D", "E", "F", "G", "2019", 1);
    when(rideDslRepository.getActiveDriverForRider(eq(candidate.getId()))).thenReturn(activeDriverDto);
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertNotNull(result.getActiveDriver());
    assertEquals(onlineDriver.getLatitude(), result.getActiveDriver().getLatitude(), 0.0);
    assertEquals(onlineDriver.getLongitude(), result.getActiveDriver().getLongitude(), 0.0);
    assertEquals(onlineDriver.getLocationObject().getCourse(), result.getActiveDriver().getCourse(), 0.0);
  }

  @Test
  public void enrichSetsActiveDriverLocationData() throws Exception {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.ACTIVE, null, null, null, 5.0, null, 34.98191,
      -97.498191, null, null, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final DispatchContext dispatchContext = new DispatchContext();
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "dispatchContext", dispatchContext
    )));
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertEquals(onlineDriver.getLatitude(), result.getActiveDriver().getLatitude(), 0.0);
    assertEquals(onlineDriver.getLongitude(), result.getActiveDriver().getLongitude(), 0.0);
    assertEquals(onlineDriver.getLocationObject().getCourse(), result.getActiveDriver().getCourse(), 0.0);
  }

  @Test
  public void enrichSetsCandidateLocationData() throws Exception {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.ACTIVE, null, null, null, 5.0, null, 34.98191,
      -97.498191, null, null, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(1L);
    candidate.setLatitude(34.616684);
    candidate.setLongitude(-97.68161615);
    dispatchContext.setCandidate(candidate);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "dispatchContext", dispatchContext
    )));
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertEquals(candidate.getLatitude(), result.getActiveDriver().getLatitude(), 0.0);
    assertEquals(candidate.getLongitude(), result.getActiveDriver().getLongitude(), 0.0);
  }

  @Test
  public void enrichSetsUpgradeRequest() throws Exception {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.ACTIVE, null, null, null, 5.0, null, 34.98191,
      -97.498191, null, null, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final DispatchContext dispatchContext = new DispatchContext();
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "dispatchContext", dispatchContext
    )));
    final RideUpgradeRequest upgradeRequest = new RideUpgradeRequest(RideUpgradeRequestStatus.ACCEPTED,
      "A", "B", BigDecimal.ONE);
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.of(upgradeRequest));

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertNotNull(result.getUpgradeRequest());
    assertEquals(upgradeRequest.getStatus(), result.getUpgradeRequest().getStatus());
    assertEquals(upgradeRequest.getTarget(), result.getUpgradeRequest().getTarget());
    assertEquals(upgradeRequest.getSource(), result.getUpgradeRequest().getSource());

  }

  @Test
  public void enrichSetsZeroTip() {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.COMPLETED, null, null, null, 5.0, null, 34.98191,
      -97.498191, null, null, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertEquals(Constants.ZERO_USD, result.getTip());
  }

  @Test
  public void enrichSetsCarType() {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.COMPLETED, null, null, null, 5.0, null, 34.98191,
      -97.498191, null, null, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.of(new CityCarType()));
    when(cityCarTypeDtoAssembler.toDto(any(CityCarType.class))).thenReturn(CityCarTypeDto.builder().build());
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertNotNull(result.getRequestedCarType());
  }

  @Test
  public void enrichSetsNullUpfrontPayment() {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.COMPLETED, null, null, null, 5.0, money(6.0), 34.98191,
      -97.498191, null, null, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(paymentConfig.isUpfrontPricingEnabled()).thenReturn(false);

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertNull(result.getUpfrontCharge());
  }

  @Test
  public void enrichSetsAcceptedOn() throws Exception {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.ACTIVE, null, null, null, 5.0, null, 34.98191,
      -97.498191, null, null, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final Date acceptedOn = new Date();
    final RideFlowContext flowContext = new RideFlowContext();
    flowContext.setAcceptedOn(acceptedOn);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "flowContext", flowContext
    )));
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertEquals(acceptedOn, result.getDriverAcceptedOn());
  }

  @Test
  public void enrichSetsETAForNotStackedRide() throws Exception {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.ACTIVE, null, null, null, 5.0, null, 34.98191,
      -97.498191, null, null, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final long eta = 200L;
    final Date acceptedOn = new Date();
    final RideFlowContext flowContext = new RideFlowContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    flowContext.setAcceptedOn(acceptedOn);
    dispatchContext.setCandidate(candidate);
    candidate.setDrivingTimeToRider(eta);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "flowContext", flowContext,
      "dispatchContext", dispatchContext
    )));
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertEquals(eta, result.getEstimatedTimeArrive().longValue());
  }

  @Test
  public void enrichSetsPrecedingForStackedRide() throws Exception {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.ACTIVE, null, null, null, 5.0, null, 34.98191,
      -97.498191, null, null, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final long eta = 200L;
    final Date acceptedOn = new Date();
    final RideFlowContext flowContext = new RideFlowContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    flowContext.setAcceptedOn(acceptedOn);
    flowContext.setStacked(true);
    dispatchContext.setCandidate(candidate);
    candidate.setDrivingTimeToRider(eta);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "flowContext", flowContext,
      "dispatchContext", dispatchContext
    )));
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());
    when(rideDslRepository.findPrecedingRide(anyLong())).thenReturn(new PrecedingRide(2L, RideStatus.ACTIVE, null, null, 34.198166, -97.684161));

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertNotNull(result.getPrecedingRide());
  }

  @Test
  public void enrichSetsETAForStackedRide() throws Exception {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.ACTIVE, null, null, null, 5.0, null, 34.98191,
      -97.498191, null, null, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final long eta = 200L;
    final Date acceptedOn = new Date();
    final RideFlowContext flowContext = new RideFlowContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    flowContext.setAcceptedOn(acceptedOn);
    flowContext.setStacked(true);
    dispatchContext.setCandidate(candidate);
    candidate.setDrivingTimeToRider(eta);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "flowContext", flowContext,
      "dispatchContext", dispatchContext
    )));
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertEquals(eta, result.getEstimatedTimeArrive().longValue());
  }

  @Test
  public void enrichSetsETC() throws Exception {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.ACTIVE, null, null, null, 5.0, null, 34.98191,
      -97.498191, 34.6816661, -97.6416164, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final Date acceptedOn = new Date();
    final RideFlowContext flowContext = new RideFlowContext();
    flowContext.setAcceptedOn(acceptedOn);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "flowContext", flowContext
    )));
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());
    when(mapService.getTimeToDriveCached(eq(source.getId()), any(LatLng.class), any(LatLng.class))).thenReturn(100L);

    final long now = Instant.now().toEpochMilli();
    final MobileRiderRideDto result = testedInstance.enrich(source);

    final long etc = result.getEstimatedTimeCompletion().getTime();
    System.out.println(now);
    System.out.println(etc);
    assertTrue(etc - now < 101000);
  }

  @Test
  public void enrichSetsDriverPhoto() throws Exception {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.ACTIVE, null, null, null, 5.0, null, 34.98191,
      -97.498191, 34.6816661, -97.6416164, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final Date acceptedOn = new Date();
    final RideFlowContext flowContext = new RideFlowContext();
    flowContext.setAcceptedOn(acceptedOn);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "flowContext", flowContext
    )));
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());
    final Document document = new Document();
    document.setDocumentUrl("url");
    when(documentDslRepository.findByAvatarAndType(anyLong(), eq(DocumentType.DRIVER_PHOTO))).thenReturn(document);

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertEquals(document.getDocumentUrl(), result.getActiveDriver().getDriver().getPhotoUrl());
  }

  @Test
  public void enrichSetsCarPhotos() throws Exception {
    MobileRiderRideDto source = new MobileRiderRideDto(1L, 1L, RideStatus.ACTIVE, null, null, null, 5.0, null, 34.98191,
      -97.498191, 34.6816661, -97.6416164, "A", null, new Address(), null, null, null, "REGULAR",
      null, null, null, 1L, null, 1L, 1L, 5.0, null, 1L, "email@email.com", "B", "C", "+15555555555", true, 1L, "COLOR",
      "AAAAA", "D", "E", "2019", 1);

    final Date acceptedOn = new Date();
    final RideFlowContext flowContext = new RideFlowContext();
    flowContext.setAcceptedOn(acceptedOn);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "flowContext", flowContext
    )));
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setLocationObject(new LocationObject(34.6166841, -97.684161, null, null, 123.0));
    when(objectLocationService.getById(eq(source.getActiveDriver().getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriver);
    when(upgradeService.getRequest(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());
    final Document front = new Document();
    front.setDocumentUrl("front-url");
    front.setDocumentType(DocumentType.CAR_PHOTO_FRONT);
    final Document back = new Document();
    back.setDocumentType(DocumentType.CAR_PHOTO_BACK);
    back.setDocumentUrl("back-url");
    when(carDocumentDslRepository.findCarPhotos(anyLong())).thenReturn(Arrays.asList(
      front, back
    ));

    final MobileRiderRideDto result = testedInstance.enrich(source);

    assertEquals(front.getDocumentUrl(), result.getActiveDriver().getSelectedCar().getPhotoUrl());
    assertEquals(2, result.getActiveDriver().getSelectedCar().getCarPhotos().size());
    assertTrue(result.getActiveDriver().getSelectedCar().getCarPhotos().containsKey(DocumentType.CAR_PHOTO_BACK));
    assertEquals(back.getDocumentUrl(), result.getActiveDriver().getSelectedCar().getCarPhotos().get(DocumentType.CAR_PHOTO_BACK));
  }
}