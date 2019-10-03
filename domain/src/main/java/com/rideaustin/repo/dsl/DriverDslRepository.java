package com.rideaustin.repo.dsl;

import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.persistence.Query;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.ride.QActiveDriver;
import com.rideaustin.model.ride.QCar;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.DriverAudited;
import com.rideaustin.model.user.QDriver;
import com.rideaustin.model.user.QUser;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.ConsoleDriverDto;
import com.rideaustin.rest.model.ListDriversParams;
import com.rideaustin.rest.model.MobileDriverDriverDto;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.QConsoleDriverDto;
import com.rideaustin.rest.model.QMobileDriverDriverDto;
import com.rideaustin.rest.model.QMobileDriverDriverDto_Car;
import com.rideaustin.service.DriverAuditedService;
import com.rideaustin.service.model.DirectConnectDriverDto;
import com.rideaustin.service.model.QDirectConnectDriverDto;
import com.rideaustin.utils.DriverUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("!itest")
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverDslRepository extends AbstractDslRepository {

  private static final String SELECT_RATING_AVERAGE = "SELECT calculate_rating_average(:defaultRating, :minimumRatingThreshold, :limit, :id) from DUAL ";
  private static final QDriver qDriver = QDriver.driver;

  private final DriverAuditedService driverAuditedService;

  @Nonnull
  @Transactional
  public <T extends BaseEntity> T saveAs(T entity, User actor) {
    T savedEntity = super.save(entity);
    makeAudit(savedEntity, actor);
    return savedEntity;
  }

  public Page<Driver> findDrivers(@Nonnull ListDriversParams params, @Nonnull PagingParams paging) {
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);
    return getPage(paging, builder, QDriver.driver);
  }

  public List<Driver> findDrivers(@Nonnull ListDriversParams params) {
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);
    return buildQuery(qDriver)
      .where(builder)
      .orderBy(qDriver.id.asc())
      .fetch();
  }

  public List<Long> findDriverIds(@Nonnull ListDriversParams params) {
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);
    return buildQuery(qDriver)
      .select(qDriver.id)
      .where(builder)
      .orderBy(qDriver.id.asc())
      .fetch();
  }

  public long getDriversCount(@Nonnull ListDriversParams params) {
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);
    return buildQuery(qDriver)
      .where(builder)
      .fetchCount();
  }

  public Long getDriverRatingCount(Driver driver) {
    QRide qRide = QRide.ride;
    return queryFactory.select(QRide.ride.driverRating.count()).from(QRide.ride).
      where(qRide.activeDriver.driver.eq(driver).and(qRide.driverRating.isNotNull()))
      .fetchOne();
  }

  public List<Ride> getDriverRatedRides(Driver driver, int limit) {
    QRide qRide = QRide.ride;

    return queryFactory.select(qRide).from(QRide.ride).
      where(qRide.activeDriver.driver.eq(driver).and(qRide.driverRating.isNotNull())
        .and(QRide.ride.driverRating.isNotNull()))
      .orderBy(QRide.ride.id.desc()).limit(limit)
      .fetch();
  }

  @Transactional(readOnly=true)
  public Driver findByUser(User user) {
    QUser qUser = QUser.user;
    return buildQuery(qDriver)
      .leftJoin(qDriver.user, qUser).fetchJoin()
      .where(
        qDriver.user.eq(user)
      )
      .orderBy(qDriver.id.asc())
      .fetchFirst();
  }

  public Driver findById(Long id) {
    QUser qUser = new QUser("qUser");
    return buildQuery(qDriver)
      .leftJoin(qDriver.cars).fetchJoin()
      .leftJoin(qDriver.user, qUser).fetchJoin()
      .where(qDriver.id.eq(id)).fetchOne();
  }

  public Driver findByEmail(String email) {
    QUser qUser = QUser.user;
    return buildQuery(qDriver)
      .leftJoin(qDriver.user, qUser).fetchJoin()
      .where(qDriver.user.email.eq(email))
      .fetchOne();
  }

  public Map<DriverOnboardingStatus, Long> findDriverCountsByStatus(Long cityId) {
    return queryFactory.select(qDriver.onboardingStatus, qDriver.count())
      .from(qDriver)
      .where(qDriver.cityId.eq(cityId))
      .groupBy(qDriver.onboardingStatus)
      .fetch()
      .stream()
      .collect(toMap(
        t -> t.get(0, DriverOnboardingStatus.class),
        t -> t.get(1, Long.class)
      ));
  }

  private <T extends BaseEntity> void makeAudit(T savedEntity, User actor) {
    DriverAudited driverAudited = new ModelMapper().map(savedEntity, DriverAudited.class);
    driverAudited.setSsn(DriverUtils.maskSsn(driverAudited.getSsn()));
    driverAuditedService.saveIfChanged(driverAudited, actor);
  }

  public Double findRatingAverage(@NonNull Double defaultRating, @NonNull Integer minimumRatingThreshold, @NonNull Integer limit, @NonNull Driver driver) {
    Query query = this.entityManager.createNativeQuery(SELECT_RATING_AVERAGE);
    query.setParameter("defaultRating", defaultRating);
    query.setParameter("minimumRatingThreshold", minimumRatingThreshold);
    query.setParameter("limit", limit);
    query.setParameter("id", driver.getId());
    return ((BigDecimal) query.getSingleResult()).doubleValue();
  }

  public MobileDriverDriverDto getCurrentDriver(User user) {
    MobileDriverDriverDto driverDto = queryFactory.from(qDriver)
      .select(new QMobileDriverDriverDto(qDriver.id, qDriver.rating, qDriver.grantedDriverTypesBitmask,
        qDriver.user.id, qDriver.user.email, qDriver.user.firstname, qDriver.user.lastname, qDriver.user.nickName,
        qDriver.user.phoneNumber, qDriver.cityId, qDriver.directConnectId, qDriver.active, qDriver.specialFlags))
      .where(qDriver.user.eq(user))
      .fetchOne();
    fillCarInfo(driverDto);
    return driverDto;
  }

  private void fillCarInfo(MobileDriverDriverDto driverDto) {
    QCar qCar = QCar.car;
    if (driverDto != null) {
      List<MobileDriverDriverDto.Car> cars = queryFactory.from(qCar)
        .select(new QMobileDriverDriverDto_Car(qCar.id, qCar.color, qCar.license, qCar.make,
          qCar.model, qCar.year, qCar.carCategoriesBitmask, qCar.selected, qCar.inspectionStatus, qCar.removed, qCar.inspectionNotes))
        .where(qCar.driver.id.eq(driverDto.getId()))
        .fetch();
      driverDto.setCars(cars);
    }
  }

  public MobileDriverDriverDto getDriverInfo(long id) {
    MobileDriverDriverDto driverDto = queryFactory.from(qDriver)
      .select(new QMobileDriverDriverDto(qDriver.id, qDriver.rating, qDriver.grantedDriverTypesBitmask,
        qDriver.user.id, qDriver.user.email, qDriver.user.firstname, qDriver.user.lastname, qDriver.user.nickName,
        qDriver.user.phoneNumber, qDriver.cityId, qDriver.directConnectId, qDriver.active, qDriver.specialFlags))
      .where(qDriver.id.eq(id))
      .fetchOne();
    fillCarInfo(driverDto);
    return driverDto;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public String getLastDCID() {
    return buildQuery(qDriver)
      .select(qDriver.directConnectId)
      .orderBy(qDriver.directConnectId.desc())
      .fetchFirst();
  }

  public Driver findByDirectConnectId(String id) {
    return buildQuery(qDriver)
      .where(qDriver.directConnectId.eq(id))
      .fetchOne();
  }

  public DirectConnectDriverDto findByDirectConnectId(String id, Integer bitmask) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    return buildQuery(qActiveDriver)
      .select(new QDirectConnectDriverDto(qActiveDriver.id, qActiveDriver.driver.id,
        qActiveDriver.driver.user.firstname, qActiveDriver.driver.user.lastname, qActiveDriver.driver.rating))
      .where(
        qActiveDriver.status.eq(ActiveDriverStatus.AVAILABLE),
        qActiveDriver.driver.directConnectId.eq(id),
        bitmaskPredicate(qActiveDriver.driver.grantedDriverTypesBitmask, bitmask)
      )
      .fetchOne();
  }

  public ConsoleDriverDto findAdminInfo(long id) {
    QUser user = qDriver.user;
    return queryFactory.from(qDriver)
      .select(new QConsoleDriverDto(qDriver.id, qDriver.cityApprovalStatus, qDriver.payoneerStatus, qDriver.directConnectId,
        qDriver.ssn, qDriver.licenseNumber, qDriver.licenseState, qDriver.activationNotes, qDriver.activationStatus, user.firstname, user.middleName, user.lastname,
        user.nickName, user.phoneNumber, user.dateOfBirth, user.address, user.email, user.gender, qDriver.specialFlags,
        qDriver.grantedDriverTypesBitmask, qDriver.rating))
      .where(qDriver.id.eq(id))
      .fetchOne();
  }
}
