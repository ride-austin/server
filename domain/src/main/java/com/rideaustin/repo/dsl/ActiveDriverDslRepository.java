package com.rideaustin.repo.dsl;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.QActiveDriver;
import com.rideaustin.model.ride.QCar;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.QDriver;
import com.rideaustin.model.user.QUser;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.ExtendedRideDriverDto;
import com.rideaustin.rest.model.QExtendedRideDriverDto;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.QDispatchCandidate;
import com.rideaustin.service.model.QOnlineDriverDto;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ActiveDriverDslRepository extends AbstractDslRepository {

  private static final QActiveDriver qActiveDriver = QActiveDriver.activeDriver;

  public Map<Long, Integer> findDriverTypeGrantedActiveDriverIds(List<Long> activeDriverIds) {
    return buildQuery(qActiveDriver)
      .select(qActiveDriver.id, qActiveDriver.driver.grantedDriverTypesBitmask)
      .where(
        qActiveDriver.id.in(activeDriverIds)
      )
      .fetch()
      .stream()
      .collect(Collectors.toMap(t -> t.get(0, Long.class), t -> safeZero(t.get(1, Integer.class))));
  }

  public List<Driver> getActiveAndRecentlyActiveDrivers(Date inactivatedAfter) {
    Predicate activeDrivers = qActiveDriver.status.in(EnumSet.complementOf(EnumSet.of(ActiveDriverStatus.INACTIVE)));
    Predicate recentlyActive = qActiveDriver.inactiveOn.goe(inactivatedAfter);

    return buildQuery(qActiveDriver)
      .select(qActiveDriver.driver)
      .distinct()
      .where(ExpressionUtils.or(activeDrivers, recentlyActive))
      .fetch();
  }

  public List<ActiveDriver> getActiveDrivers(Driver driver, Date from, Date to) {
    return buildQuery(qActiveDriver)
      .where(qActiveDriver.driver.eq(driver)
        .and(qActiveDriver.inactiveOn.between(from, to)
          .or(qActiveDriver.createdDate.between(from, to))))
      .fetch();
  }

  @Transactional(readOnly = true)
  public List<ActiveDriver> getActiveDrivers(User user) {
    QDriver qDriver = QDriver.driver;
    QUser qUser = QUser.user;
    return buildQuery(qActiveDriver)
      .leftJoin(qActiveDriver.driver, qDriver).fetchJoin()
      .leftJoin(qDriver.user, qUser).fetchJoin()
      .where(
        qActiveDriver.driver.user.eq(user),
        qActiveDriver.status.ne(ActiveDriverStatus.INACTIVE)
      )
      .orderBy(qActiveDriver.id.desc())
      .fetch();
  }

  @Transactional(readOnly = true)
  public ActiveDriver findById(Long activeDriverId) {
    return get(activeDriverId, ActiveDriver.class);
  }

  @Transactional(readOnly = true)
  public ActiveDriver findByIdWithDependencies(Long activeDriverId) {
    QDriver qDriver = QDriver.driver;
    QUser qUser = QUser.user;
    QCar qCar = QCar.car;
    return buildQuery(qActiveDriver)
      .join(qActiveDriver.driver, qDriver).fetchJoin()
      .join(qDriver.user, qUser).fetchJoin()
      .join(qActiveDriver.selectedCar, qCar).fetchJoin()
      .select(qActiveDriver)
      .where(qActiveDriver.id.eq(activeDriverId))
      .fetchOne();
  }

  public DispatchCandidate findDispatchCandidate(long id) {
    return queryFactory.from(qActiveDriver)
      .select(new QDispatchCandidate(qActiveDriver.id, qActiveDriver.driver.id,
        qActiveDriver.selectedCar.license, qActiveDriver.driver.user.id, qActiveDriver.status))
      .where(qActiveDriver.id.eq(id))
      .fetchOne();
  }

  public List<ActiveDriver> findByIds(Set<Long> activeDriverIds) {
    return buildQuery(qActiveDriver)
      .where(
        qActiveDriver.id.in(activeDriverIds)
      ).orderBy(qActiveDriver.id.asc())
      .fetch();
  }

  public Iterable<ActiveDriver> findActiveDriversEndedBetween(Date start, Date end, Collection<Driver> drivers) {
    return buildQuery(qActiveDriver)
      .where(
        qActiveDriver.createdDate.loe(end),
        qActiveDriver.updatedDate.goe(start),
        qActiveDriver.driver.in(drivers)
      )
      .fetch();
  }

  @Transactional
  public void setRidingDriverAsAvailable(long id) {
    changeStatus(id, ActiveDriverStatus.RIDING, ActiveDriverStatus.AVAILABLE);
  }

  @Transactional
  public void setAvailableDriverAsRiding(long id) {
    changeStatus(id, ActiveDriverStatus.AVAILABLE, ActiveDriverStatus.RIDING);
  }

  public OnlineDriverDto findByDirectConnectId(String directConnectId, Integer bitmask) {
    return buildQuery(qActiveDriver)
      .select(new QOnlineDriverDto(qActiveDriver.id, qActiveDriver.status, qActiveDriver.driver.id,
        qActiveDriver.driver.user.id,
        getFullName(qActiveDriver.driver.user),
        qActiveDriver.driver.user.phoneNumber, qActiveDriver.selectedCar.license))
      .where(
        qActiveDriver.driver.directConnectId.eq(directConnectId),
        qActiveDriver.status.eq(ActiveDriverStatus.AVAILABLE),
        bitmaskPredicate(qActiveDriver.driver.grantedDriverTypesBitmask, bitmask)
      )
      .fetchOne();
  }

  public ActiveDriver findByUserAndNotInactive(User user) {
    return buildQuery(qActiveDriver)
      .where(
        qActiveDriver.driver.user.eq(user),
        qActiveDriver.status.in(EnumSet.complementOf(EnumSet.of(ActiveDriverStatus.INACTIVE)))
      ).fetchFirst();
  }

  @Transactional
  public void setAway(long id) {
    queryFactory.update(qActiveDriver)
      .set(qActiveDriver.status, ActiveDriverStatus.AWAY)
      .where(qActiveDriver.id.eq(id))
      .execute();
  }

  @Transactional
  public void setInactive(long id) {
    queryFactory.update(qActiveDriver)
      .set(Arrays.asList(qActiveDriver.status, qActiveDriver.inactiveOn), Arrays.asList(ActiveDriverStatus.INACTIVE, new Date()))
      .where(qActiveDriver.id.eq(id))
      .execute();
  }

  public ExtendedRideDriverDto findExtendedRideDriverInfo(Long activeDriverId) {
    return buildQuery(qActiveDriver)
      .select(new QExtendedRideDriverDto(qActiveDriver.driver.id, qActiveDriver.driver.user.firstname, qActiveDriver.driver.user.lastname,
        qActiveDriver.driver.user.phoneNumber))
      .where(qActiveDriver.id.eq(activeDriverId))
      .fetchOne();
  }

  private void changeStatus(long id, ActiveDriverStatus currentStatus, ActiveDriverStatus newStatus) {
    queryFactory
      .update(qActiveDriver)
      .where(
        qActiveDriver.id.eq(id),
        qActiveDriver.status.eq(currentStatus)
      )
      .set(qActiveDriver.status, newStatus)
      .execute();
  }

}
