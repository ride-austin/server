package com.rideaustin.service;

import static com.rideaustin.service.FareServiceTestUtils.stubCarType;
import static com.rideaustin.service.FareTestConstants.stubCityCarType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.assemblers.CampaignBannerDtoAssembler;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.EstimatedFareDTO;
import com.rideaustin.service.airport.AirportService;
import com.rideaustin.service.model.DistanceTime;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.promocodes.PromocodeUseRequest;
import com.rideaustin.service.promocodes.PromocodeUseResult;
import com.rideaustin.service.surgepricing.SurgePricingService;

public class FareEstimateServiceTest {

  @Mock
  private MapService mapService;
  @Mock
  private AirportService airportService;
  @Mock
  private CarTypeService carTypeService;
  @Mock
  private CampaignService campaignService;
  @Mock
  private PromocodeService promocodeService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private SurgePricingService surgePricingService;
  @Mock
  private CampaignBannerDtoAssembler campaignBannerDtoAssembler;

  private FareEstimateService testedInstance;

  private static final long DISTANCE = 3000L;
  private static final long TIME = 300L;
  private static final double EXPECTED_ESTIMATE = 8.12;

  private Ride ride = new Ride();

  private LatLng pickupLatLng = new LatLng(10d, 10d);
  private LatLng dropoffLatLng = new LatLng(20d, 20d);

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    FareServiceTestUtils.prepareRide(ride, pickupLatLng, dropoffLatLng);

    DistanceTime distanceTime = new DistanceTime(DISTANCE, TIME);
    when(mapService.computeDistanceTime(any(), any())).thenReturn(distanceTime);

    when(carTypeService.getCityCarTypeWithFallback(any(CarType.class), any())).thenReturn(stubCityCarType());
    when(carTypeService.getCityCarType(anyString(), anyLong())).thenReturn(stubCityCarType());
    when(campaignService.findMatchingCampaignForRide(ride)).thenReturn(Optional.empty());
    when(campaignService.findEligibleCampaign(any(Date.class), any(), any(), any(), any(), any())).thenReturn(Optional.empty());
    when(mapService.computeDistanceTime(any(), any())).thenReturn(distanceTime);
    final User user = new User();
    user.addAvatar(new Rider());
    when(currentUserService.getUser()).thenReturn(user);
    when(promocodeService.usePromocode(any(PromocodeUseRequest.class), eq(true))).thenReturn(new PromocodeUseResult());
    setupSurge(Constants.NEUTRAL_SURGE_FACTOR);
    FareServiceTestUtils.setupAirport(airportService);

    testedInstance = new FareEstimateService(mapService, airportService, carTypeService, campaignService,
      promocodeService, currentUserService, surgePricingService, campaignBannerDtoAssembler);
  }

  @Test
  public void testEstimateFareReturnsEmptyOnMapException() throws MapException {
    when(mapService.computeDistanceTime(pickupLatLng, pickupLatLng)).thenThrow(new MapException("Map exception", new Exception()));

    Optional<EstimatedFareDTO> result = testedInstance.estimateFare(pickupLatLng, pickupLatLng, stubCarType(), 1L);

    assertFalse(result.isPresent());
  }

  @Test
  public void testEstimateFareReturnsEmptyOnEmptyCityCarType() {
    when(carTypeService.getCityCarType(anyString(), anyLong())).thenReturn(Optional.empty());

    Optional<EstimatedFareDTO> result = testedInstance.estimateFare(pickupLatLng, pickupLatLng, stubCarType(), 1L);

    assertFalse(result.isPresent());
  }

  @Test
  public void testEstimateFare() {
    Optional<EstimatedFareDTO> estimatedFare = testedInstance.estimateFare(pickupLatLng, pickupLatLng, stubCarType(), 1L);

    assertEstimatedFare(estimatedFare, EXPECTED_ESTIMATE);
  }

  @Test
  public void testEstimateFareWithSurge() {
    setupSurge(BigDecimal.valueOf(1.5));

    Optional<EstimatedFareDTO> estimatedFare = testedInstance.estimateFare(pickupLatLng, pickupLatLng, stubCarType(), 1L);

    assertEstimatedFare(estimatedFare, 11.68);
  }

  @Test
  public void testEstimateFareWithAirportPickupFee() {
    testEstimateFareWithAirportFee(pickupLatLng);
  }

  @Test
  public void testEstimateFareWithAirportDropoffFee() {
    testEstimateFareWithAirportFee(pickupLatLng);
  }

  @Test
  public void testEstimateFareWithAirportPickupFeeAndSurge() {
    testEstimateFareWithAirportFeeAndSurge(pickupLatLng);
  }

  @Test
  public void testEstimateFareWithAirportDropoffFeeAndSurge() {
    testEstimateFareWithAirportFeeAndSurge(dropoffLatLng);
  }

  @Test
  public void testEstimateFareForRideReturnsEmptyIfNoEndPointIsSet() {
    ride.setEndLocationLat(null);
    ride.setEndLocationLong(null);
    Optional<FareDetails> result = testedInstance.estimateFare(ride);

    assertFalse(result.isPresent());
  }

  @Test
  public void testEstimateFareForRideReturnsEmptyOnEmptyCityCarType() {
    ride.setEndLocationLat(0.0);
    ride.setEndLocationLong(0.0);
    when(carTypeService.getCityCarTypeWithFallback(any(CarType.class), anyLong())).thenReturn(Optional.empty());

    Optional<FareDetails> result = testedInstance.estimateFare(ride);

    assertFalse(result.isPresent());
  }

  @Test
  public void testEstimateFareForRideReturnsEmptyOnMapException() throws MapException {
    ride.setEndLocationLat(0.0);
    ride.setEndLocationLong(0.0);
    when(mapService.computeDistanceTime(any(), any())).thenThrow(new MapException("Map exception", new Exception()));

    Optional<FareDetails> result = testedInstance.estimateFare(ride);

    assertFalse(result.isPresent());
  }

  @Test
  public void testEstimateFareForRideSetsEstimatedFareField() {
    ride.setEndLocationLat(0.0);
    ride.setEndLocationLong(0.0);
    Optional<FareDetails> result = testedInstance.estimateFare(ride);

    assertTrue(result.isPresent());
    assertThat(result.get().getEstimatedFare().getAmount().doubleValue(), is(closeTo(EXPECTED_ESTIMATE, 0.0)));
  }

  private void testEstimateFareWithAirportFeeAndSurge(LatLng airportLocation) {
    FareServiceTestUtils.setupAirport(true, airportLocation, airportService);
    setupSurge(BigDecimal.valueOf(1.5));

    Optional<EstimatedFareDTO> estimatedFare = testedInstance.estimateFare(pickupLatLng, dropoffLatLng, stubCarType(), 1L);

    assertEstimatedFare(estimatedFare, 14.71);
  }

  private void testEstimateFareWithAirportFee(LatLng airportLocation) {
    FareServiceTestUtils.setupAirport(true, airportLocation, airportService);

    Optional<EstimatedFareDTO> estimatedFare = testedInstance.estimateFare(pickupLatLng, dropoffLatLng, stubCarType(), 1L);

    assertEstimatedFare(estimatedFare, 11.15);
  }

  private void setupSurge(BigDecimal surgeFactor) {
    when(surgePricingService.getSurgeFactor(any(), any(), any())).thenReturn(surgeFactor);
    ride.setSurgeFactor(surgeFactor);
  }


  private void assertEstimatedFare(Optional<EstimatedFareDTO> estimatedFare, double expectedAmount) {
    assertTrue(estimatedFare.isPresent());
    assertThat(estimatedFare.get().getDefaultEstimate().getTotalFare().getAmount().doubleValue(), is(closeTo(expectedAmount, 0.0)));
    assertThat(estimatedFare.get().getDuration(), is(TIME));
  }

}