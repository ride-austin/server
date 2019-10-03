package com.rideaustin.service.promocodes;

import static com.rideaustin.Constants.City.AUSTIN;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Optional;

import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.CarTypeService;
import com.rideaustin.test.setup.C1177039Setup;

public abstract class BaseC1177039Test extends AbstractNonTxPromocodeTest<C1177039Setup> {

  @Inject
  private CarTypeService carTypeService;

  protected BigDecimal cancellationFee;
  protected Double promoCredit;
  protected final Long austin = AUSTIN.getId();

  protected ActiveDriver activeDriver;
  protected Rider rider;
  protected PromocodeRedemption redemption;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.setup = createSetup();
    cancellationFee = getCancellationFeeForCityCarType(austin, setup.getRegularCarType());
    promoCredit = setup.getMinimumFareForCityCarType(austin, setup.getRegularCarType()).doubleValue();
    activeDriver = setup.getActiveDriver();
    rider = setup.getRider();
    redemption = setup.getRedemption();
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "cancellationChargeFreePeriod", 20);
  }

  protected BigDecimal getCancellationFeeForCityCarType(Long cityId, CarType carType) {
    Optional<CityCarType> cityCarType = carTypeService.getCityCarType(carType, cityId);
    if (cityCarType.isPresent()) {
      return cityCarType.get().getCancellationFee().getAmount();
    } else {
      throw new AssertionError(String.format("Should have found car type %s for %s", carType.getCarCategory(), cityId));
    }
  }

  @NotNull
  protected Long setupRide() throws Exception {
    LatLng location = locationProvider.getCenter();
    driverAction.goOnline(activeDriver.getDriver().getEmail(), location);
    driverAction.locationUpdate(activeDriver, location.lat, location.lng)
      .andExpect(status().isOk());
    Long ride = riderAction.requestRide(rider.getEmail(), location);
    awaitDispatch(activeDriver, ride);
    return ride;
  }
}
