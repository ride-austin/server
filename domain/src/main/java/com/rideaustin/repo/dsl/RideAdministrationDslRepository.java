package com.rideaustin.repo.dsl;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.rideaustin.Constants;
import com.rideaustin.model.QSession;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.QActiveDriver;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.ride.QRiderOverride;
import com.rideaustin.model.user.QDriver;
import com.rideaustin.model.user.QRider;
import com.rideaustin.model.user.QRiderCard;
import com.rideaustin.model.user.QUser;
import com.rideaustin.rest.model.CompactRideDto;
import com.rideaustin.rest.model.ConsoleRideDto;
import com.rideaustin.rest.model.ExtendedRideDto;
import com.rideaustin.rest.model.ListRidesParams;
import com.rideaustin.rest.model.MapInfoDto;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.QCompactRideDto;
import com.rideaustin.rest.model.QConsoleRideDto;
import com.rideaustin.rest.model.QExtendedRideDto;
import com.rideaustin.rest.model.QMapInfoDto;
import com.rideaustin.rest.model.QRideHistoryDto;
import com.rideaustin.rest.model.RideHistoryDto;

@Repository
public class RideAdministrationDslRepository extends AbstractDslRepository {

  private static final QRide qRide = QRide.ride;
  private static final QDriver qDriver = QDriver.driver;

  public Page<ExtendedRideDto> findRidesExtended(@Nonnull ListRidesParams params, @Nonnull PagingParams paging) {
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);
    QUser riderUser = QUser.user;
    QSession riderSession = new QSession("riderSession");
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    JPAQuery<ExtendedRideDto> query = queryFactory
      .select(new QExtendedRideDto(qRide.id, qRide.rider.id,
        qRiderOverride.firstName.coalesce(riderUser.firstname),
        qRiderOverride.lastName.coalesce(riderUser.lastname), qRide.requestedCarType.carCategory, qRide.startedOn,
        qRide.completedOn, qRide.distanceTravelled, qRide.status, qRide.riderSession.userAgent,
        qRide.start.zipCode, qRide.start.address, qRide.end.zipCode, qRide.end.address, qActiveDriver.id))
      .from(qRide)
      .leftJoin(qRide.rider, QRider.rider)
      .leftJoin(qRide.rider.user, riderUser)
      .leftJoin(qRide.riderSession, riderSession)
      .leftJoin(qRide.activeDriver, qActiveDriver)
      .leftJoin(qRide.riderOverride, qRiderOverride)
      .where(builder);

    long total = query.fetchCount();
    List<ExtendedRideDto> content = appendPagingParams(query, paging, qRide).fetch();

    return new PageImpl<>(content, paging.toPageRequest(), total);
  }

  public ConsoleRideDto findAdminRideInfo(long id) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    QRiderCard qRiderCard = QRiderCard.riderCard;
    QUser qRiderUser = QUser.user;
    QUser qDriverUser = new QUser("driverUser");
    QSession qRiderSession = QSession.session;
    QSession qDriverSession = new QSession("driverSession");
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    QRider qRider = QRider.rider;
    return queryFactory.from(qRide)
      .leftJoin(qRide.activeDriver, qActiveDriver)
      .leftJoin(qActiveDriver.driver, qDriver)
      .leftJoin(qDriver.user, qDriverUser)
      .leftJoin(qRide.rider, qRider)
      .leftJoin(qRider.user, qRiderUser)
      .leftJoin(qRide.riderOverride, qRiderOverride)
      .leftJoin(qRider.primaryCard, qRiderCard)
      .leftJoin(qRide.riderSession, qRiderSession)
      .leftJoin(qRide.driverSession, qDriverSession)
      .select(new QConsoleRideDto(qRide.id, qRide.status, qRide.startLocationLat, qRide.startLocationLong, qRide.endLocationLat,
        qRide.endLocationLong, qRide.start.address, qRide.fareDetails, qRide.startedOn, qRide.driverAcceptedOn, qRide.completedOn,
        qRide.cancelledOn, qRide.requestedOn, qRide.end.address, qDriver.id, qDriverUser.photoUrl, getFullName(qDriverUser), qRider.id, qRiderUser.photoUrl,
        getFullName(qRiderUser, qRiderOverride.firstName, qRiderOverride.lastName), qRiderCard.cardBrand, qRiderCard.cardNumber, qRide.requestedCarType.carCategory,
        qRide.driverReachedOn, qRide.tippedOn, qRide.surgeFactor, qRide.distanceTravelled, qRide.cityId, qDriverSession.userAgent,
        qRiderSession.userAgent))
      .where(qRide.id.eq(id))
      .fetchOne();
  }

  public Page<RideHistoryDto> findAdminRideInfoPage(ListRidesParams params, PagingParams paging) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    final QUser qUser = QUser.user;
    JPAQuery<RideHistoryDto> query = queryFactory.from(qRide)
      .leftJoin(qRide.activeDriver, qActiveDriver)
      .leftJoin(qRide.activeDriver.driver, qDriver)
      .leftJoin(qRide.activeDriver.driver.user, qUser)
      .leftJoin(qRide.riderOverride, qRiderOverride)
      .select(new QRideHistoryDto(qRide.id, qRide.status, qRide.start.address, qRide.end.address, getFullName(qUser),
        getFullName(qRide.rider.user, qRiderOverride.firstName, qRiderOverride.lastName), qRide.startedOn, qRide.completedOn,
        qRide.cancelledOn, qRide.fareDetails.estimatedFare,
        qRide.fareDetails.totalFare, qRide.fareDetails.tip, qRide.tippedOn))
      .where(builder);

    long total = query.fetchCount();
    List<RideHistoryDto> content = appendPagingParams(query, paging, qRide).fetch();

    return new PageImpl<>(content, paging.toPageRequest(), total);
  }

  public Page<CompactRideDto> findRidesCompact(ListRidesParams params, PagingParams paging) {
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);

    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    QUser qUser = QUser.user;
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    JPAQuery<CompactRideDto> query = queryFactory.select(new QCompactRideDto(
      qRide.id, qRide.rider.id, qRiderOverride.firstName.coalesce(qRide.rider.user.firstname),
      qRiderOverride.lastName.coalesce(qRide.rider.user.lastname), qRide.requestedCarType.carCategory,
      qDriver.id, qUser.firstname, qUser.lastname, qRide.startedOn, qRide.completedOn,
      qRide.distanceTravelled.multiply(Constants.MILES_PER_METER), qRide.fareDetails.tip, qRide.tippedOn, qRide.cancelledOn)
    ).from(qRide)
      .leftJoin(qRide.activeDriver, qActiveDriver)
      .leftJoin(qRide.activeDriver.driver, qDriver)
      .leftJoin(qRide.activeDriver.driver.user, qUser)
      .leftJoin(qRide.riderOverride, qRiderOverride)
      .where(builder);

    long total = query.fetchCount();
    List<CompactRideDto> content = appendPagingParams(query, paging, qRide).fetch();

    return new PageImpl<>(content, paging.toPageRequest(), total);
  }

  public List<MapInfoDto> findMapInfo(Long cityId) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    return buildQuery(qRide)
      .leftJoin(qRide.activeDriver, qActiveDriver)
      .select(new QMapInfoDto(qRide.id, qActiveDriver.id, qRide.startLocationLat, qRide.startLocationLong, qRide.status, qRide.rider.id))
      .where(
        qRide.status.in(EnumSet.of(RideStatus.REQUESTED, RideStatus.DRIVER_ASSIGNED, RideStatus.ACTIVE, RideStatus.DRIVER_REACHED)),
        qRide.cityId.eq(cityId)
      )
      .fetch();
  }

}
