package com.rideaustin.test.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.ApiClientDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.repo.dsl.TermsDslRepository;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.test.fixtures.ActiveDriverFixture;
import com.rideaustin.test.fixtures.AdministratorFixture;
import com.rideaustin.test.fixtures.ApiClientFixture;
import com.rideaustin.test.fixtures.AreaGeometryFixture;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.CarTypeFixture;
import com.rideaustin.test.fixtures.CardFixture;
import com.rideaustin.test.fixtures.CharityFixture;
import com.rideaustin.test.fixtures.DispatchFixture;
import com.rideaustin.test.fixtures.DriverFixture;
import com.rideaustin.test.fixtures.PromocodeFixture;
import com.rideaustin.test.fixtures.PromocodeRedemptionFixture;
import com.rideaustin.test.fixtures.RideFixture;
import com.rideaustin.test.fixtures.RideTrackFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.SessionFixture;
import com.rideaustin.test.fixtures.SurgeAreaFixture;
import com.rideaustin.test.fixtures.SurgeFactorFixture;
import com.rideaustin.test.fixtures.TermsAcceptanceFixture;
import com.rideaustin.test.fixtures.UserFixture;
import com.rideaustin.test.fixtures.check.UserChecker;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverStatisticFixtureProvider;
import com.rideaustin.test.fixtures.providers.PromocodeFixtureProvider;
import com.rideaustin.test.fixtures.providers.PromocodeRedemptionFixtureProvider;
import com.rideaustin.test.fixtures.providers.RideFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;
import com.rideaustin.test.fixtures.providers.UserFixtureProvider;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.test.utils.RandomUtils;
import com.rideaustin.utils.DateUtils;

@Configuration
public class FixtureConfig {

  @Bean
  public UserFixture riderUser(UserDslRepository repository, PasswordEncoder passwordEncoder) {
    UserFixture userFixture = UserFixture.builder()
      .phoneNumber(RandomUtils.randomPhoneNumber())
      .email("rider@user.com")
      .firstName(RandomUtils.randomName())
      .lastName(RandomUtils.randomName())
      .password(passwordEncoder.encode("rider@user.com"))
      .avatarBitmask(AvatarType.RIDER.toBitMask())
      .build();
    userFixture.setRecordChecker(new UserChecker(repository));
    return userFixture;
  }

  @Bean
  public UserFixture driverUser(UserDslRepository repository, PasswordEncoder passwordEncoder) {
    UserFixture userFixture = UserFixture.builder()
      .phoneNumber(RandomUtils.randomPhoneNumber())
      .email("driver@user.com")
      .firstName(RandomUtils.randomName())
      .lastName(RandomUtils.randomName())
      .password(passwordEncoder.encode("driver@user.com"))
      .avatarBitmask(AvatarType.DRIVER.toBitMask() | AvatarType.RIDER.toBitMask())
      .build();
    userFixture.setRecordChecker(new UserChecker(repository));
    return userFixture;
  }

  @Bean
  public UserFixture apiClientUser(UserDslRepository repository, PasswordEncoder passwordEncoder) {
    UserFixture userFixture = UserFixture.builder()
      .phoneNumber(RandomUtils.randomPhoneNumber())
      .email("apiclient@acme.com")
      .firstName(RandomUtils.randomName())
      .lastName(RandomUtils.randomName())
      .password(passwordEncoder.encode("apiclient@acme.com"))
      .avatarBitmask(AvatarType.API_CLIENT.toBitMask())
      .build();
    userFixture.setRecordChecker(new UserChecker(repository));
    return userFixture;
  }

  @Bean
  @Primary
  public CardFixture validCard() {
    return CardFixture.builder()
      .cardExpired(false)
      .build();
  }

  @Bean
  public CardFixture invalidCard() {
    return CardFixture.builder()
      .cardExpired(true)
      .build();
  }

  @Bean
  public CharityFixture charity() {
    return CharityFixture.builder().createCharity(true).build();
  }

  @Bean
  public CharityFixture noCharity() {
    return CharityFixture.builder().createCharity(false).build();
  }

  @Bean
  @Primary
  public RiderFixture simpleRiderWithCharity(@Qualifier("riderUser") UserFixture userFixture, CardFixture cardFixture,
    @Qualifier("charity") CharityFixture charityFixture, RiderDslRepository repository
  ) {
    RiderFixture fixture = RiderFixture.builder()
      .userFixture(userFixture)
      .primaryCardFixture(cardFixture)
      .charityFixture(charityFixture)
      .cityId(1L)
      .build();
    fixture.setRepository(repository);
    return fixture;
  }

  @Bean
  public ApiClientFixture apiClient(@Qualifier("apiClientUser") UserFixture userFixture, ApiClientDslRepository repository) {
    return ApiClientFixture.builder()
      .userFixture(userFixture)
      .repository(repository)
      .build();
  }

  @Bean
  public RiderFixture simpleRiderWithoutCharity(@Qualifier("riderUser") UserFixture userFixture, CardFixture cardFixture,
    RiderDslRepository repository) {
    RiderFixture fixture = RiderFixture.builder()
      .userFixture(userFixture)
      .primaryCardFixture(cardFixture)
      .cityId(1L)
      .build();
    fixture.setRepository(repository);
    return fixture;
  }

  @Bean
  public RiderFixture riderWithTwoCards(@Qualifier("riderUser") UserFixture userFixture, @Qualifier("validCard") CardFixture validCardFixture,
    @Qualifier("invalidCard") CardFixture invalidCardFixture, RiderDslRepository repository) {
    RiderFixture fixture = RiderFixture.builder()
      .userFixture(userFixture)
      .primaryCardFixture(validCardFixture)
      .secondaryCardFixture(invalidCardFixture)
      .cityId(1L)
      .build();
    fixture.setRepository(repository);
    return fixture;
  }

  @Bean
  public RiderFixture riderWithoutCard(@Qualifier("riderUser") UserFixture userFixture, RiderDslRepository repository) {
    RiderFixture fixture = RiderFixture.builder()
      .userFixture(userFixture)
      .cityId(1L)
      .build();
    fixture.setRepository(repository);
    return fixture;
  }

  @Bean
  public UserFixture adminUser(UserDslRepository repository, PasswordEncoder passwordEncoder) {
    UserFixture userFixture = UserFixture.builder()
      .phoneNumber(RandomUtils.randomPhoneNumber())
      .email("admin@user.com")
      .firstName(RandomUtils.randomName())
      .lastName(RandomUtils.randomName())
      .password(passwordEncoder.encode("admin@user.com"))
      .avatarBitmask(AvatarType.DRIVER.toBitMask() | AvatarType.RIDER.toBitMask() | AvatarType.ADMIN.toBitMask())
      .build();
    userFixture.setRecordChecker(new UserChecker(repository));
    return userFixture;
  }

  @Bean
  public AdministratorFixture administratorFixture(@Qualifier("adminUser") UserFixture userFixture) {
    return AdministratorFixture.builder()
      .userFixture(userFixture)
      .build();
  }

  @Bean
  public DriverFixture simpleDriver(
    @Qualifier("driverUser") UserFixture userFixture,
    @Qualifier("suvCar") CarFixture carFixture,
    TermsAcceptanceFixture acceptanceFixture
  ) {
    return DriverFixture.builder()
      .userFixture(userFixture)
      .carFixture(carFixture)
      .acceptanceFixture(acceptanceFixture)
      .acceptTerms(true)
      .build();
  }

  @Bean
  public ActiveDriverFixture activeDriverFixture(DriverFixture driverFixture) {
    return ActiveDriverFixture.builder().driverFixture(driverFixture).build();
  }

  @Bean
  public ActiveDriverFixture availableActiveDriver(DriverFixture driverFixture) {
    return ActiveDriverFixture.builder()
      .driverFixture(driverFixture)
      .status(ActiveDriverStatus.AVAILABLE)
      .build();
  }

  @Bean
  public ActiveDriverFixture requestedActiveDriver(DriverFixture driverFixture) {
    return ActiveDriverFixture.builder()
      .driverFixture(driverFixture)
      .status(ActiveDriverStatus.REQUESTED)
      .build();
  }

  @Bean
  public ActiveDriverFixture ridingActiveDriver(DriverFixture driverFixture) {
    return ActiveDriverFixture.builder()
      .driverFixture(driverFixture)
      .status(ActiveDriverStatus.RIDING)
      .build();
  }

  @Bean
  @Primary
  public CarFixture regularCar() {
    return CarFixture.builder()
      .categoriesBitmask(1)
      .build();
  }

  @Bean
  public CarFixture suvCar() {
    return CarFixture.builder()
      .categoriesBitmask(3)
      .build();
  }

  @Bean
  public CarFixture hondaCar() {
    return CarFixture.builder()
      .categoriesBitmask(8)
      .build();
  }

  @Bean
  public CarFixture premiumCar() {
    return CarFixture.builder()
      .categoriesBitmask(5)
      .build();
  }

  @Bean
  public CarFixture luxuryCar() {
    return CarFixture.builder()
      .categoriesBitmask(9)
      .build();
  }

  @Bean
  public CarFixture allTypesCar() {
    return CarFixture.builder()
      .categoriesBitmask(31)
      .build();
  }

  @Bean
  public TermsAcceptanceFixture termsAcceptanceFixture(TermsDslRepository repository) {
    return new TermsAcceptanceFixture(repository);
  }

  @Bean
  @Primary
  public CarTypeFixture carTypeRegularFixture() {
    return CarTypeFixture.builder().carCategory("REGULAR").build();
  }

  @Bean
  public CarTypeFixture carTypeSuvFixture() {
    return CarTypeFixture.builder().carCategory("SUV").build();
  }

  @Bean
  public CarTypeFixture carTypePremiumFixture() {
    return CarTypeFixture.builder().carCategory("PREMIUM").build();
  }

  @Bean
  public CarTypeFixture carTypeHondaFixture() {
    return CarTypeFixture.builder().carCategory("HONDA").build();
  }

  @Bean
  public RideFixture completedRide(RiderFixture riderFixture, ActiveDriverFixture activeDriverFixture,
    CarTypeFixture carTypeFixture) {
    return RideFixture.builder()
      .endLocation(new LatLng(30.2747789,-97.7384711))
      .activeDriverFixture(activeDriverFixture)
      .carTypeFixture(carTypeFixture)
      .cityId(1L)
      .riderFixture(riderFixture)
      .status(RideStatus.COMPLETED)
      .surgeFactor(1.0)
      .tip(3.0)
      .build();
  }

  @Bean
  public RideFixture activeRide(RiderFixture riderFixture, @Qualifier("ridingActiveDriver") ActiveDriverFixture activeDriverFixture,
    CarTypeFixture carTypeFixture, RideTrackFixture rideTrackFixture) {
    return RideFixture.builder()
      .activeDriverFixture(activeDriverFixture)
      .carTypeFixture(carTypeFixture)
      .cityId(1L)
      .riderFixture(riderFixture)
      .rideTrackFixture(rideTrackFixture)
      .status(RideStatus.ACTIVE)
      .surgeFactor(1.0)
      .tip(0.0)
      .build();
  }

  @Bean
  public RideFixture dispatchedRide(RiderFixture riderFixture, CarTypeFixture carTypeFixture, DispatchFixture dispatchFixture) {
    return RideFixture.builder()
      .carTypeFixture(carTypeFixture)
      .dispatchFixture(dispatchFixture)
      .cityId(1L)
      .riderFixture(riderFixture)
      .status(RideStatus.REQUESTED)
      .surgeFactor(1.0)
      .tip(0.0)
      .build();
  }

  @Bean
  public DispatchFixture dispatchFixture(@Qualifier("requestedActiveDriver") ActiveDriverFixture activeDriverFixture) {
    return DispatchFixture.builder()
      .activeDriverFixture(activeDriverFixture)
      .build();
  }

  @Bean
  @Primary
  public RideTrackFixture shortRideTrack() {
    Date startTrack = DateUtils.localDateTimeToDate(LocalDateTime.of(2017, 5, 1, 8, 9, 36), ZoneId.systemDefault());
    return new RideTrackFixture()
      .add(new LatLng(30.280482569872802, -97.71838366985321), 0L, startTrack, 0)
      .add(new LatLng(30.281097, -97.706803), 1493626236, startTrack, 60)
      .add(new LatLng(30.302907, -97.699158), 1493626243, startTrack, 89)
      .add(new LatLng(30.302907, -97.699158), 1493626245, startTrack, 90)
      .add(new LatLng(30.302907, -97.699158), 1493626259, startTrack, 99)
      .add(new LatLng(30.302907, -97.699158), 1493626293, startTrack, 105);
  }

  @Bean
  public RideTrackFixture oneRideTrack() {
    Date startTrack = DateUtils.localDateTimeToDate(LocalDateTime.of(2017, 5, 1, 8, 9, 36), ZoneId.systemDefault());
    return new RideTrackFixture()
      .add(new LatLng(30.240549, -97.785888), 0L, startTrack, 0);
  }

  @Bean
  public RideFixture driverCancelledRide(RiderFixture riderFixture, ActiveDriverFixture activeDriverFixture, CarTypeFixture carTypeFixture) {
    return RideFixture.builder()
      .activeDriverFixture(activeDriverFixture)
      .carTypeFixture(carTypeFixture)
      .cityId(1L)
      .riderFixture(riderFixture)
      .status(RideStatus.DRIVER_CANCELLED)
      .surgeFactor(1.0)
      .tip(0.0)
      .build();
  }

  @Bean
  @Primary
  public PromocodeFixture publicPromocode() {
    return PromocodeFixture.builder().build();
  }

  @Bean
  public PromocodeFixture applicableToFeesPromocode() {
    return PromocodeFixture.builder()
      .applicableToFees(true)
      .value(50.0)
      .cityBitMask(3)
      .carTypeBitMask(31)
      .build();
  }

  @Bean
  public PromocodeFixture nonApplicableToFeesPromocode() {
    return PromocodeFixture.builder()
      .value(50.0)
      .cityBitMask(3)
      .carTypeBitMask(31)
      .build();
  }

  @Bean
  public PromocodeFixture fixedAmountPromocode() {
    return PromocodeFixture.builder()
      .value(20.0)
      .cityBitMask(3)
      .carTypeBitMask(31)
      .maxUsePerAccount(4)
      .cappedAmountPerUse(5.0)
      .build();
  }

  @Bean
  public PromocodeFixtureProvider promocodeFixtureProvider(RiderFixture riderFixture, EntityManager entityManager) {
    return new PromocodeFixtureProvider(riderFixture, entityManager);
  }

  @Bean
  public PromocodeRedemptionFixture invalidRedemption(PromocodeFixture promocodeFixture, RiderFixture riderFixture) {
    return PromocodeRedemptionFixture.builder()
      .active(true)
      .valid(false)
      .promocodeFixture(promocodeFixture)
      .riderFixture(riderFixture)
      .build();
  }

  @Bean
  @Primary
  public PromocodeRedemptionFixture validRedemption(PromocodeFixture promocodeFixture, RiderFixture riderFixture) {
    return PromocodeRedemptionFixture.builder()
      .active(true)
      .valid(true)
      .promocodeFixture(promocodeFixture)
      .riderFixture(riderFixture)
      .build();
  }

  @Bean
  public RideFixture riderCancelledRide(RiderFixture riderFixture, @Qualifier("availableActiveDriver") ActiveDriverFixture activeDriverFixture, CarTypeFixture carTypeFixture) {
    return RideFixture.builder()
      .activeDriverFixture(activeDriverFixture)
      .carTypeFixture(carTypeFixture)
      .cityId(1L)
      .riderFixture(riderFixture)
      .status(RideStatus.RIDER_CANCELLED)
      .surgeFactor(1.0)
      .tip(0.0)
      .build();
  }

  @Bean
  public RideFixture driverAssignedRide(RiderFixture riderFixture, @Qualifier("ridingActiveDriver") ActiveDriverFixture activeDriverFixture,
    CarTypeFixture carTypeFixture) {
    return RideFixture.builder()
      .activeDriverFixture(activeDriverFixture)
      .carTypeFixture(carTypeFixture)
      .cityId(1L)
      .riderFixture(riderFixture)
      .status(RideStatus.DRIVER_ASSIGNED)
      .surgeFactor(1.0)
      .tip(0.0)
      .build();
  }

  @Bean
  public SessionFixture app310Session() {
    return new SessionFixture("RideAustin_iOS_3.1.0");
  }

  @Bean
  public SessionFixture app320Session() {
    return new SessionFixture("RideAustin_iOS_3.2.0");
  }

  @Bean
  public AreaGeometryFixture areaGeometryFixture() {
    return new AreaGeometryFixture();
  }

  @Bean
  public SurgeFactorFixture regularNeutralSurgeFactor() {
    return SurgeFactorFixture.builder().carType(TestUtils.REGULAR).build();
  }

  @Bean
  public SurgeFactorFixture regularDoubleSurgeFactor() {
    return SurgeFactorFixture.builder().carType(TestUtils.REGULAR).value(BigDecimal.valueOf(2.0)).build();
  }

  @Bean
  public SurgeAreaFixture neutralSurgeAreaFixture(AreaGeometryFixture areaGeometryFixture, @Qualifier("regularNeutralSurgeFactor") SurgeFactorFixture factor) {
    SurgeAreaFixture fixture = new SurgeAreaFixture(areaGeometryFixture);
    fixture.addFactor(factor);
    return fixture;
  }

  @Bean
  public SurgeAreaFixture doubleSurgeAreaFixture(AreaGeometryFixture areaGeometryFixture, @Qualifier("regularDoubleSurgeFactor") SurgeFactorFixture factor) {
    SurgeAreaFixture fixture = new SurgeAreaFixture(areaGeometryFixture);
    fixture.addFactor(factor);
    return fixture;
  }

  @Bean
  public DriverStatisticFixtureProvider driverStatisticFixtureProvider(EntityManager entityManager) {
    return new DriverStatisticFixtureProvider(entityManager);
  }

  @Bean
  public ActiveDriverFixtureProvider activeDriverFixtureProvider(DriverFixtureProvider driverFixtureProvider,
    EntityManager entityManager, @Qualifier("suvCar") CarFixture defaultCarFixture) {
    return new ActiveDriverFixtureProvider(entityManager, driverFixtureProvider, defaultCarFixture);
  }

  @Bean
  public DriverFixtureProvider driverFixtureProvider(TermsAcceptanceFixture acceptanceFixture, CarFixture carFixture,
    PasswordEncoder passwordEncoder, UserDslRepository repository, EntityManager entityManager) {
    return new DriverFixtureProvider(acceptanceFixture, carFixture, passwordEncoder, repository, entityManager);
  }

  @Bean
  public RideFixtureProvider rideFixtureProvider(RiderFixture riderFixture, DriverFixture driverFixture,
    CarTypeFixture carTypeFixture, RideTrackFixture rideTrackFixture, ActiveDriverFixtureProvider activeDriverFixtureProvider,
    EntityManager entityManager, DriverDslRepository driverDslRepository) {
    return new RideFixtureProvider(riderFixture, driverFixture, carTypeFixture, rideTrackFixture,
      activeDriverFixtureProvider, entityManager, driverDslRepository);
  }

  @Bean
  public UserFixtureProvider userFixtureProvider(EntityManager entityManager, PasswordEncoder passwordEncoder,
    UserDslRepository userDslRepository) {
    return new UserFixtureProvider(entityManager, passwordEncoder, userDslRepository);
  }

  @Bean
  public RiderFixtureProvider riderFixtureProvider(UserFixtureProvider userFixtureProvider, CardFixture cardFixture,
    @Qualifier("charity") CharityFixture charityFixture, RiderDslRepository riderDslRepository, EntityManager entityManager) {
    return new RiderFixtureProvider(userFixtureProvider, cardFixture, charityFixture, riderDslRepository, entityManager);
  }

  @Bean
  public PromocodeRedemptionFixtureProvider redemptionFixtureProvider(RiderFixture riderFixture, PromocodeFixture promocodeFixture,
    EntityManager entityManager) {
    return new PromocodeRedemptionFixtureProvider(riderFixture, promocodeFixture, entityManager);
  }
}
