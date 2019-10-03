package com.rideaustin.test.fixtures;

import java.util.Date;
import java.util.Optional;

import com.google.common.collect.Sets;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.test.fixtures.check.RecordChecker;
import com.rideaustin.utils.RandomString;

public class DriverFixture extends AbstractFixture<Driver> {

  private AvatarDocumentFixture avatarDocumentFixture;
  private UserFixture userFixture;
  private CarFixture carFixture;
  private TermsAcceptanceFixture acceptanceFixture;
  private boolean acceptTerms;
  private int grantedDriverTypesBitmask;
  private CityApprovalStatus cityApprovalStatus;

  DriverFixture(UserFixture userFixture, CarFixture carFixture, TermsAcceptanceFixture acceptanceFixture,
    boolean acceptTerms, int grantedDriverTypesBitmask, RecordChecker<Driver> recordChecker, AvatarDocumentFixture avatarDocumentFixture,
    CityApprovalStatus cityApprovalStatus) {
    this.userFixture = userFixture;
    this.carFixture = carFixture;
    this.acceptanceFixture = acceptanceFixture;
    this.acceptTerms = acceptTerms;
    this.grantedDriverTypesBitmask = grantedDriverTypesBitmask;
    this.avatarDocumentFixture = avatarDocumentFixture;
    this.cityApprovalStatus = cityApprovalStatus;
    Optional.ofNullable(recordChecker).ifPresent(checker -> this.recordChecker = checker);
  }

  public static DriverFixtureBuilder builder() {
    return new DriverFixtureBuilder();
  }

  @Override
  protected Driver createObject() {
    Driver driver = Driver.builder()
      .agreementDate(new Date())
      .ssn(RandomString.generate(9))
      .cityApprovalStatus(cityApprovalStatus)
      .grantedDriverTypesBitmask(grantedDriverTypesBitmask)
      .cityId(1L)
      .onboardingStatus(DriverOnboardingStatus.ACTIVE)
      .activationStatus(DriverActivationStatus.ACTIVE)
      .specialFlags(0)
      .rating(5.0)
      .build();
    driver.setActive(true);
    return driver;
  }

  @Override
  public Driver getFixture() {
    Driver driver = createObject();
    userFixture.addAvatar(driver);
    driver.setUser(userFixture.getFixture());
    Optional<Driver> existing = recordChecker.getIfExists(driver);
    if (existing.isPresent()) {
      return existing.get();
    }

    driver = entityManager.merge(driver);
    carFixture.setDriver(driver);
    driver.setCars(Sets.newHashSet(carFixture.getFixture()));
    driver = entityManager.merge(driver);
    if (avatarDocumentFixture != null) {
      avatarDocumentFixture.setEntityManager(entityManager);
      avatarDocumentFixture.setAvatar(driver);
      avatarDocumentFixture.getFixture();
    }
    if (acceptTerms) {
      acceptTerms(driver);
    }
    entityManager.flush();
    return driver;
  }

  private void acceptTerms(Driver driver) {
    acceptanceFixture.setDriver(driver);
    acceptanceFixture.getFixture();
  }

  public static class DriverFixtureBuilder {

    private UserFixture userFixture;
    private CarFixture carFixture;
    private TermsAcceptanceFixture acceptanceFixture;
    private boolean acceptTerms;
    private int grantedDriverTypesBitmask = 0;
    private CityApprovalStatus cityApprovalStatus = CityApprovalStatus.APPROVED;
    private AvatarDocumentFixture avatarDocumentFixture = new AvatarDocumentFixture(DocumentType.DRIVER_PHOTO, Constants.DEFAULT_DRIVER_PHOTO);
    private RecordChecker<Driver> recordChecker;

    public DriverFixtureBuilder userFixture(UserFixture userFixture) {
      this.userFixture = userFixture;
      return this;
    }

    public DriverFixtureBuilder carFixture(CarFixture carFixture) {
      this.carFixture = carFixture;
      return this;
    }

    public DriverFixtureBuilder acceptanceFixture(TermsAcceptanceFixture acceptanceFixture) {
      this.acceptanceFixture = acceptanceFixture;
      return this;
    }

    public DriverFixtureBuilder acceptTerms(boolean acceptTerms) {
      this.acceptTerms = acceptTerms;
      return this;
    }

    public DriverFixtureBuilder grantedDriverTypesBitmask(int grantedDriverTypesBitmask) {
      this.grantedDriverTypesBitmask = grantedDriverTypesBitmask;
      return this;
    }

    public DriverFixtureBuilder avatarDocumentFixture(AvatarDocumentFixture avatarDocumentFixture) {
      this.avatarDocumentFixture = avatarDocumentFixture;
      return this;
    }

    public DriverFixtureBuilder cityApprovalStatus(CityApprovalStatus cityApprovalStatus) {
      this.cityApprovalStatus = cityApprovalStatus;
      return this;
    }

    public DriverFixtureBuilder recordChecker(RecordChecker<Driver> recordChecker) {
      this.recordChecker = recordChecker;
      return this;
    }

    public DriverFixture build() {
      return new DriverFixture(userFixture, carFixture, acceptanceFixture, acceptTerms, grantedDriverTypesBitmask,
        recordChecker, avatarDocumentFixture, cityApprovalStatus);
    }

    public String toString() {
      return "com.rideaustin.test.fixtures.DriverFixture.DriverFixtureBuilder()";
    }
  }
}
