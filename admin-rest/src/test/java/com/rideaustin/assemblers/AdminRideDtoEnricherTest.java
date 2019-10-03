package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;

import com.rideaustin.Constants;
import com.rideaustin.StubStateMachineContext;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.enums.CampaignCoverageType;
import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.rest.model.ConsoleRideDto;
import com.rideaustin.service.CampaignService;
import com.rideaustin.service.CarTypeService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;

public class AdminRideDtoEnricherTest {

  private AdminRideDtoEnricher testedInstance;

  @Mock
  private CampaignService campaignService;
  @Mock
  private CarTypeService carTypeService;
  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private Environment environment;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new AdminRideDtoEnricher(campaignService, carTypeService, contextAccess, environment);
  }

  @Test
  public void enrichSkipsNull() {
    final ConsoleRideDto result = testedInstance.enrich(null);

    assertNull(result);
  }

  @Test
  public void enrichFillsContextVariables() throws Exception {
    Map<Object, Object> state = new HashMap<>();
    final RideFlowContext rideFlowContext = new RideFlowContext();
    final Date acceptedOn = Date.from(Instant.now().minus(10, ChronoUnit.SECONDS));
    final Date reachedOn = Date.from(Instant.now().minus(5, ChronoUnit.SECONDS));
    final Date startedOn = new Date();
    rideFlowContext.setAcceptedOn(acceptedOn);
    rideFlowContext.setReachedOn(reachedOn);
    rideFlowContext.setStartedOn(startedOn);
    state.put("flowContext", rideFlowContext);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(state));

    final ConsoleRideDto source = createObject(RideStatus.ACTIVE, null);
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());
    final ConsoleRideDto result = testedInstance.enrich(source);

    assertEquals(acceptedOn, result.getDriverAcceptedOn());
    assertEquals(reachedOn, result.getDriverReachedOn());
    assertEquals(startedOn, result.getStartedOn());
  }

  @Test
  public void enrichFillsCarType() throws Exception {
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(new HashMap<>()));

    final ConsoleRideDto source = createObject(RideStatus.ACTIVE, null);
    final CityCarType cityCarType = new CityCarType();
    final CarType carType = new CarType();
    final String title = "Cartype";
    final Money ratePerMile = Money.of(CurrencyUnit.USD, BigDecimal.ONE);
    final Money ratePerMinute = Money.of(CurrencyUnit.USD, BigDecimal.ONE);
    final Money minimumFare = Money.of(CurrencyUnit.USD, BigDecimal.ONE);
    carType.setTitle(title);
    cityCarType.setCarType(carType);
    cityCarType.setRatePerMile(ratePerMile);
    cityCarType.setRatePerMinute(ratePerMinute);
    cityCarType.setMinimumFare(minimumFare);
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.of(cityCarType));
    final ConsoleRideDto result = testedInstance.enrich(source);

    assertEquals(title, result.getRequestedCarType().getTitle());
    assertEquals(ratePerMile, result.getRequestedCarType().getRatePerMile());
    assertEquals(ratePerMinute, result.getRequestedCarType().getRatePerMinute());
    assertEquals(minimumFare, result.getRequestedCarType().getMinimumFare());
  }

  @Test
  public void enrichSetsCampaignInfoForCompleted() throws Exception {
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(new HashMap<>()));

    final Money totalCharge = Money.of(CurrencyUnit.USD, BigDecimal.TEN);
    final ConsoleRideDto source = createObject(RideStatus.COMPLETED, totalCharge);

    final String campaignName = "Campaign";
    final Campaign campaign = new Campaign();
    campaign.setCoverageType(CampaignCoverageType.FULL);
    campaign.setName(campaignName);
    when(campaignService.findExistingCampaignForRide(anyLong())).thenReturn(Optional.of(campaign));
    when(carTypeService.getCityCarTypeWithFallback(anyString(), anyLong())).thenReturn(Optional.empty());

    final ConsoleRideDto result = testedInstance.enrich(source);

    assertEquals(campaignName, result.getCampaign());
    assertEquals(totalCharge, result.getCampaignCoverage());
    assertEquals(Constants.ZERO_USD, result.getTotalChargeOverride());
  }

  private ConsoleRideDto createObject(final RideStatus rideStatus, Money totalCharge) {
    long id = 1L;
    double startLocationLat = 34.6456;
    double startLocationLong = -97.3645;
    Double endLocationLat = null;
    Double endLocationLong = null;
    String startAddress = null;
    FareDetails fareDetails = new FareDetails();
    if (totalCharge != null) {
      fareDetails.setTotalFare(totalCharge);
    }
    Date requestedOn = new Date();
    String endAddress = null;
    long driverId = 1L;
    String driverPhotoUrl = null;
    String driverFullName = "A B";
    long riderId = 1L;
    String riderPhotoUrl = null;
    String riderFullName = "B C";
    CardBrand cardBrand = CardBrand.VISA;
    String cardNumber = "4564";
    String requestedCarType = "REGULAR";
    BigDecimal surgeFactor = BigDecimal.ONE;
    BigDecimal distanceTravelled = null;
    long cityId = 1L;
    String driverUA = "RideAustinDriver_iOS_5.1.0 (707)";
    String riderUA = "RideAustin_iOS_5.1.0 (707)";

    return new ConsoleRideDto(id, rideStatus, startLocationLat, startLocationLong, endLocationLat, endLocationLong,
      startAddress, fareDetails, null, null, null, null, requestedOn,
      endAddress, driverId, driverPhotoUrl, driverFullName, riderId, riderPhotoUrl, riderFullName, cardBrand,
      cardNumber, requestedCarType, null, null, surgeFactor, distanceTravelled, cityId, driverUA,
      riderUA);
  }

}