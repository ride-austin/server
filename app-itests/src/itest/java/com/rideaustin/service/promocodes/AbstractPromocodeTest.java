package com.rideaustin.service.promocodes;

import static com.rideaustin.Constants.City.AUSTIN;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.Constants;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.service.CarTypeService;
import com.rideaustin.test.actions.AdministratorAction;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.CacheRefreshDisabled;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.AdministratorFixture;
import com.rideaustin.test.fixtures.CarTypeFixture;
import com.rideaustin.test.fixtures.PromocodeRedemptionFixture;
import com.rideaustin.test.fixtures.RideTrackFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.PromocodeFixtureProvider;
import com.rideaustin.test.fixtures.providers.PromocodeRedemptionFixtureProvider;
import com.rideaustin.test.fixtures.providers.RideFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;
import com.rideaustin.test.stubs.ConfigurationItemCache;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
@CacheRefreshDisabled
public abstract class AbstractPromocodeTest extends ITestProfileSupport {

  @Inject
  private CarTypeService carTypeService;

  @Inject
  protected RideFixtureProvider rideFixtureProvider;

  @Inject
  protected RiderFixtureProvider riderFixtureProvider;

  @Inject
  protected PromocodeRedemptionFixtureProvider redemptionFixtureProvider;
  protected PromocodeRedemption redemption;

  @Inject
  protected PromocodeFixtureProvider promocodeFixtureProvider;

  @Inject
  protected CarTypeFixture carTypeRegularFixture;

  @Inject
  @Qualifier("carTypeSuvFixture")
  protected CarTypeFixture carTypeSuvFixture;

  @Inject
  @Qualifier("carTypePremiumFixture")
  protected CarTypeFixture carTypePremiumFixture;

  @Inject
  @Qualifier("carTypeHondaFixture")
  protected CarTypeFixture carTypeHondaFixture;

  @Inject
  protected DriverAction driverAction;

  @Inject
  protected RiderAction riderAction;

  @Inject
  protected AdministratorFixture administratorFixture;
  protected Administrator administrator;

  @Inject
  protected AdministratorAction administratorAction;

  @Inject
  @Qualifier("oneRideTrack")
  protected RideTrackFixture oneRideTrackFixture;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Before
  public void promoSetUp() throws Exception {
    super.setUp();
    updateRidePaymentDelay();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "driverStats", "enabled", false);
  }

  protected void updateRidePaymentDelay() {
    jdbcTemplate.update("UPDATE configuration_items SET configuration_value = '{\"rideTipLimit\": 300, \"ridePaymentDelay\": 0}' " +
      "WHERE client_type = 'RIDER' and configuration_key = 'tipping'");
    try {
      configurationItemCache.refreshCache();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected Ride newRide(RideStatus rideStatus, RiderFixture riderFixture) {
    return newRide(rideStatus, riderFixture, AUSTIN.getId());
  }

  protected Ride newRide(RideStatus rideStatus, RiderFixture riderFixture, Long cityId) {
    return newRide(rideStatus, riderFixture, cityId, carTypeRegularFixture);
  }

  protected Ride newRide(RideStatus rideStatus, RiderFixture riderFixture, Long cityId, CarTypeFixture carTypeFixture) {
    return rideFixtureProvider.create(rideStatus, false, builder ->
      builder.riderFixture(riderFixture).cityId(cityId).carTypeFixture(carTypeFixture).rideTrackFixture(oneRideTrackFixture)).getFixture();
  }

  protected PromocodeRedemption newRedemption(RiderFixture riderFixture, Double value) {
    return newRedemption(riderFixture, value, null);
  }

  protected PromocodeRedemption newRedemption(RiderFixture riderFixture, Double value, Integer cityBitMask) {
    return newRedemption(riderFixture, value, cityBitMask, null);
  }

  protected PromocodeRedemption newRedemption(RiderFixture riderFixture, Double value, Integer cityBitMask, Integer carTypeBitMask) {
    return newRedemption(riderFixture, value, cityBitMask, carTypeBitMask, Boolean.FALSE);
  }

  protected PromocodeRedemption newRedemption(RiderFixture riderFixture, Double value, Integer cityBitMask,
    Integer carTypeBitMask, Boolean nextTripOnly) {
    return newRedemption(riderFixture, value, cityBitMask, carTypeBitMask, nextTripOnly, 10);
  }

  protected PromocodeRedemption newRedemption(RiderFixture riderFixture, Double value, Integer cityBitMask,
    Integer carTypeBitMask, Boolean nextTripOnly, int maxUsePerAccount) {
    PromocodeRedemptionFixture redemptionFixture = redemptionFixtureProvider.create(
      builder -> builder
        .riderFixture(riderFixture)
        .promocodeFixture(promocodeFixtureProvider.create(value, null,
          promoBuilder ->
            promoBuilder
              .cityBitMask(cityBitMask)
              .carTypeBitMask(carTypeBitMask)
              .nextTripOnly(nextTripOnly)
              .maxUsePerAccount(maxUsePerAccount))));

    return redemptionFixture.getFixture();
  }

  protected BigDecimal getMinimumFareForCityCarType(CarTypeFixture carTypeFixture, Long cityId) {
    CarType carType = carTypeFixture.getFixture();
    Optional<CityCarType> cityCarType = carTypeService.getCityCarType(carType, cityId);
    if (cityCarType.isPresent()) {
      return cityCarType.get().getMinimumFare().getAmount();
    } else {
      throw new AssertionError(String.format("Should have found car type %s for %s", carType.getCarCategory(), cityId));
    }
  }

  public static void assertUsed(Ride ride, PromocodeRedemption redemption, int expectedUsageCount, BigDecimal... previousUsages) {
    final BigDecimal currentUsage = ride.getFreeCreditCharged().getAmount();
    BigDecimal previousTotal = Arrays.stream(previousUsages).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertThat(redemption.getRemainingValue())
      .isEqualByComparingTo(redemption.getOriginalValue().subtract(previousTotal).subtract(currentUsage).max(BigDecimal.ZERO));
    assertThat(redemption.getNumberOfTimesUsed()).isEqualTo(expectedUsageCount);
    assertThat(ride.getFreeCreditCharged().getAmount())
      .isEqualByComparingTo(redemption.getOriginalValue().subtract(previousTotal).subtract(redemption.getRemainingValue()).max(BigDecimal.ZERO));
  }

  public static void assertNotUsed(Ride ride, PromocodeRedemption redemption, int expectedUsageCount, BigDecimal... previousUsages) {
    BigDecimal previousTotal = Arrays.stream(previousUsages).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertThat(redemption.getRemainingValue())
      .isEqualByComparingTo(redemption.getOriginalValue().subtract(previousTotal).max(BigDecimal.ZERO));
    assertThat(redemption.getNumberOfTimesUsed()).isEqualTo(expectedUsageCount);
    assertThat(ride.getFreeCreditCharged().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  public static void assertNotUsed(Ride ride, BigDecimal expectedRideCharge, PromocodeRedemption redemption,
    int expectedUsageCount, BigDecimal... previousUsages) {
    BigDecimal previousTotal = Arrays.stream(previousUsages).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertThat(redemption.getRemainingValue())
      .isEqualByComparingTo(redemption.getOriginalValue().subtract(previousTotal).max(BigDecimal.ZERO));
    assertThat(redemption.getNumberOfTimesUsed()).isEqualTo(expectedUsageCount);
    assertThat(ride.getFreeCreditCharged().getAmount()).isEqualByComparingTo(expectedRideCharge);
  }
}
