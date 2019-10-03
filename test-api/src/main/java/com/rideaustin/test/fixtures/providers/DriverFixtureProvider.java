package com.rideaustin.test.fixtures.providers;

import javax.persistence.EntityManager;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.DriverFixture;
import com.rideaustin.test.fixtures.TermsAcceptanceFixture;
import com.rideaustin.test.fixtures.UserFixture;
import com.rideaustin.test.fixtures.check.UserChecker;
import com.rideaustin.test.utils.RandomUtils;

public class DriverFixtureProvider {

  private final TermsAcceptanceFixture acceptanceFixture;
  private final CarFixture carFixture;
  private final PasswordEncoder passwordEncoder;
  private final UserDslRepository repository;
  private final EntityManager entityManager;

  public DriverFixtureProvider(TermsAcceptanceFixture acceptanceFixture, CarFixture carFixture,
    PasswordEncoder passwordEncoder, UserDslRepository repository, EntityManager entityManager) {
    this.acceptanceFixture = acceptanceFixture;
    this.carFixture = carFixture;
    this.passwordEncoder = passwordEncoder;
    this.repository = repository;
    this.entityManager = entityManager;
  }

  public DriverFixture create(){
    return create(carFixture);
  }

  public DriverFixture create(CarFixture carFixture) {
    return create(carFixture, 0);
  }

  public DriverFixture create(CarFixture carFixture, int driverType) {
    return create(carFixture, driverType, CityApprovalStatus.APPROVED);
  }

  public DriverFixture create(CarFixture carFixture, int driverType, CityApprovalStatus cityApprovalStatus) {
    DriverFixture fixture = DriverFixture.builder()
      .userFixture(createUserFixture())
      .carFixture(carFixture)
      .acceptanceFixture(acceptanceFixture)
      .grantedDriverTypesBitmask(driverType)
      .acceptTerms(true)
      .cityApprovalStatus(cityApprovalStatus)
      .build();
    fixture.setEntityManager(entityManager);
    return fixture;
  }

  private UserFixture createUserFixture() {
    String email = RandomUtils.randomEmail("driver");
    UserFixture userFixture = UserFixture.builder()
      .phoneNumber(RandomUtils.randomPhoneNumber())
      .email(email)
      .firstName(RandomUtils.randomName())
      .lastName(RandomUtils.randomName())
      .password(passwordEncoder.encode(email))
      .avatarBitmask(AvatarType.DRIVER.toBitMask() | AvatarType.RIDER.toBitMask())
      .build();

    userFixture.setRecordChecker(new UserChecker(repository));
    userFixture.setEntityManager(entityManager);
    return userFixture;
  }
}
