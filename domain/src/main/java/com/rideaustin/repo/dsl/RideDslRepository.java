package com.rideaustin.repo.dsl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.rideaustin.Constants;
import com.rideaustin.model.Charity;
import com.rideaustin.model.City;
import com.rideaustin.model.MessagingRideInfoDTO;
import com.rideaustin.model.QMessagingRideInfoDTO;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.QActiveDriver;
import com.rideaustin.model.ride.QCar;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.ride.QRiderOverride;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.QDriver;
import com.rideaustin.model.user.QUser;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.DirectConnectHistoryDto;
import com.rideaustin.rest.model.DispatcherAccountRideDto;
import com.rideaustin.rest.model.DriverRide;
import com.rideaustin.rest.model.ListRidesParams;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.MobileDriverRideEventPayloadDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.MobileRiderRideDto.ActiveDriverDto;
import com.rideaustin.rest.model.MobileRiderRideDto.PrecedingRide;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.QDirectConnectHistoryDto;
import com.rideaustin.rest.model.QDispatcherAccountRideDto;
import com.rideaustin.rest.model.QDriverRide;
import com.rideaustin.rest.model.QMobileDriverRideDto;
import com.rideaustin.rest.model.QMobileDriverRideEventPayloadDto;
import com.rideaustin.rest.model.QMobileRiderRideDto;
import com.rideaustin.rest.model.QMobileRiderRideDto_ActiveDriverDto;
import com.rideaustin.rest.model.QMobileRiderRideDto_PrecedingRide;
import com.rideaustin.service.model.AssociatedRide;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.ETACalculationInfo;
import com.rideaustin.service.model.ETCCalculationInfo;
import com.rideaustin.service.model.QAssociatedRide;
import com.rideaustin.service.model.QDispatchCandidate;
import com.rideaustin.service.model.QETACalculationInfo;
import com.rideaustin.service.model.QETCCalculationInfo;
import com.rideaustin.service.model.context.QRideRequestContext;
import com.rideaustin.service.model.context.RideRequestContext;

@Repository
public class RideDslRepository extends AbstractDslRepository {

  private static final QRide qRide = QRide.ride;
  private static final QDriver qDriver = QDriver.driver;

  public Ride findOne(Long id) {
    return get(id, Ride.class);
  }

  public Ride findOneWithRider(Long id) {
    return buildQuery(qRide)
      .leftJoin(qRide.rider).fetchJoin()
      .where(qRide.id.eq(id))
      .fetchOne();
  }

  public Ride findActiveByActiveDriver(ActiveDriver activeDriver) {
    return buildQuery(qRide)
      .where(
        qRide.activeDriver.eq(activeDriver),
        qRide.status.eq(RideStatus.ACTIVE)
      )
      .select(qRide)
      .fetchOne();
  }

  public List<Ride> ridesList(ListRidesParams params) {
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);
    return buildQuery(qRide)
      .where(builder)
      .orderBy(qRide.id.asc())
      .fetch();
  }

  public Long countCompletedRidesPerRider(Rider rider) {
    return buildQuery(qRide)
      .where(qRide.rider.id.eq(rider.getId())
        .and(qRide.status.eq(RideStatus.COMPLETED)))
      .fetchCount();
  }

  public Boolean isInRide(Rider rider) {
    RideStatus status = queryFactory.from(qRide)
      .select(qRide.status)
      .where(qRide.rider.eq(rider))
      .orderBy(qRide.id.desc())
      .limit(1L)
      .fetchOne();
    return Sets.newHashSet(RideStatus.ONGOING_RIDER_STATUSES).contains(status);
  }

  @Transactional(readOnly = true)
  public Map<Long, RideStatus> getStatuses(Collection<Long> rideIds) {
    return buildQuery(qRide)
      .select(qRide.id, qRide.status)
      .where(qRide.id.in(rideIds))
      .fetch()
      .stream()
      .collect(Collectors.toMap(
        t -> t.get(qRide.id),
        t -> t.get(qRide.status)
      ));
  }

  public List<Ride> getRidesByStatusAndCreateDate(Date createdOnBefore, Set<RideStatus> rideStatuses) {

    BooleanBuilder where = new BooleanBuilder();

    if (createdOnBefore != null) {
      where.and(qRide.createdDate.loe(createdOnBefore));
    }
    if (CollectionUtils.isNotEmpty(rideStatuses)) {
      where.and(qRide.status.in(rideStatuses));
    }

    return buildQuery(qRide)
      .where(where)
      .fetch();
  }

  @Transactional(readOnly = true)
  public Ride getOngoingRideForDriver(User user) {
    return buildQuery(qRide)
      .where(qRide.activeDriver.driver.user.eq(user)
        .and(qRide.status.in(RideStatus.ONGOING_DRIVER_STATUSES)))
      .fetchOne();
  }

  @Transactional(readOnly = true)
  public Long getOngoingRideDriverId(User user) {
    return buildQuery(qRide)
      .where(qRide.activeDriver.driver.user.eq(user)
        .and(qRide.status.in(RideStatus.ONGOING_DRIVER_STATUSES)))
      .select(qRide.activeDriver.id)
      .fetchOne();
  }

  public List<Ride> getDriverEarnings(Collection<Driver> drivers, Instant completedOnAfter, Instant completedOnBefore,
    City city) {
    JPAQuery<Ride> query = buildQuery(qRide);
    query.where(driverEarningsCriteria(drivers, completedOnAfter, completedOnBefore, city));
    return query.fetch();
  }

  public Page<DriverRide> getPageableDriverEarnings(Driver driver, Instant completedOnAfter,
    Instant completedOnBefore, PagingParams paging) {
    BooleanBuilder builder = driverEarningsCriteria(Collections.singleton(driver), completedOnAfter, completedOnBefore, null);
    QCar selectedCar = new QCar("qSelectedCar");
    QCar defaultCar = new QCar("qDefaultCar");
    JPAQuery<DriverRide> query = queryFactory.select(
      new QDriverRide(qRide.id, qRide.status, qRide.startLocationLat, qRide.startLocationLong,
        qRide.start, qRide.endLocationLat, qRide.endLocationLong, qRide.end, qRide.startedOn, qRide.completedOn,
        qRide.cancelledOn, qRide.fareDetails, qRide.driverRating, qRide.requestedCarType, qRide.rideMap, qRide.surgeFactor,
        selectedCar.make.coalesce(defaultCar.make),
        selectedCar.model.coalesce(defaultCar.model)))
      .from(qRide)
      .leftJoin(selectedCar).on(selectedCar.id.eq(qRide.activeDriver.selectedCar.id))
      .leftJoin(defaultCar).on(defaultCar.driver.id.eq(qRide.activeDriver.driver.id).and(defaultCar.selected.isTrue()))
      .where(builder);

    long total = query.fetchCount();
    List<DriverRide> content = appendPagingParams(query, paging, qRide).fetch();

    return new PageImpl<>(content, paging.toPageRequest(), total);
  }

  public List<AssociatedRide> findOngoingRideByParticipantPhoneNumber(String phoneNumber) {
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    return queryFactory.select(new QAssociatedRide(qRide.id, qRide.status,
      qRiderOverride.phoneNumber.coalesce(qRide.rider.user.phoneNumber),
      qRide.activeDriver.driver.user.phoneNumber)
    ).from(qRide)
      .leftJoin(qRide.riderOverride, qRiderOverride)
      .where(
        qRide.status.in(RideStatus.ONGOING_DRIVER_STATUSES),
        qRide.rider.user.phoneNumber.eq(phoneNumber)
          .or(qRide.activeDriver.driver.user.phoneNumber.eq(phoneNumber))
          .or(qRiderOverride.phoneNumber.eq(phoneNumber))
      )
      .orderBy(qRide.id.asc())
      .fetch();
  }

  private BooleanBuilder driverEarningsCriteria(Collection<Driver> drivers, Instant completedOnAfter,
    Instant completedOnBefore, City city) {
    BooleanBuilder builder = new BooleanBuilder();
    if (CollectionUtils.isNotEmpty(drivers)) {
      builder.and(qRide.activeDriver.driver.in(drivers));
    }
    if (city != null) {
      builder.and(qRide.cityId.eq(city.getId()));
    }
    Predicate completed = ExpressionUtils.allOf(
      qRide.completedOn.loe(Date.from(completedOnBefore)),
      qRide.completedOn.goe(Date.from(completedOnAfter)),
      qRide.fareDetails.totalFare.goe(Constants.ZERO_USD),
      qRide.status.eq(RideStatus.COMPLETED)
    );

    Predicate cancelled = ExpressionUtils.allOf(
      qRide.cancelledOn.goe(Date.from(completedOnAfter)),
      qRide.cancelledOn.loe(Date.from(completedOnBefore)),
      qRide.fareDetails.totalFare.gt(Constants.ZERO_USD),
      qRide.status.in(RideStatus.RIDER_CANCELLED, RideStatus.DRIVER_CANCELLED)
    );

    builder.and(ExpressionUtils.or(completed, cancelled));
    return builder;
  }

  public List<Ride> listPendingPaymentsRides(Rider rider) {
    JPAQuery<Ride> query = buildQuery(qRide);
    BooleanExpression paymentPredicate = qRide.paymentStatus.in(PaymentStatus.UNPAID, PaymentStatus.BLOCKED);
    if (rider != null) {
      paymentPredicate = paymentPredicate.and(qRide.rider.eq(rider));
    }
    query.where(paymentPredicate);
    return query.fetch();
  }

  public Ride getRideForTrackingShareToken(String token) {
    return buildQuery(qRide)
      .leftJoin(qRide.activeDriver).fetchJoin()
      .where(qRide.trackingShareToken.eq(token))
      .fetchOne();
  }

  public Ride findByDriverAndStatus(ActiveDriver activeDriver, Set<RideStatus> statuses) {
    return buildQuery(qRide)
      .where(
        qRide.activeDriver.eq(activeDriver),
        qRide.status.in(statuses)
      )
      .fetchOne();
  }

  public Collection<Ride> findByRiderAndStatus(Rider rider, RideStatus status) {
    return buildQuery(qRide)
      .where(
        qRide.rider.eq(rider),
        qRide.status.eq(status)
      )
      .fetch();
  }

  public List<Ride> findByRiderAndStatus(Rider rider, Set<RideStatus> statuses) {
    return buildQuery(qRide)
      .where(
        qRide.rider.eq(rider),
        qRide.status.in(statuses)
      )
      .fetch();
  }

  public List<Ride> findByActiveDriverAndStatuses(ActiveDriver activeDriver, Set<RideStatus> statuses) {
    return buildQuery(qRide)
      .where(
        qRide.activeDriver.eq(activeDriver),
        qRide.status.in(statuses)
      )
      .fetch();
  }

  public MessagingRideInfoDTO getMessagingRideInfo(long rideId) {
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    return queryFactory.select(new QMessagingRideInfoDTO(qRiderOverride.phoneNumber.coalesce(qRide.rider.user.phoneNumber),
      qRide.activeDriver.driver.user.firstname, qRide.activeDriver.selectedCar.license,
      qRide.activeDriver.selectedCar.color, qRide.activeDriver.selectedCar.make, qRide.activeDriver.selectedCar.model,
      qRide.cityId))
      .from(qRide)
      .leftJoin(qRide.riderOverride, qRiderOverride)
      .where(qRide.id.eq(rideId))
      .fetchOne();
  }

  public Rider findRider(Long rideId) {
    return buildQuery(qRide)
      .select(qRide.rider)
      .where(qRide.id.eq(rideId))
      .fetchOne();
  }

  public User findRiderUser(Long rideId) {
    return buildQuery(qRide)
      .select(qRide.rider.user)
      .where(qRide.id.eq(rideId))
      .fetchOne();
  }

  public Charity findCharity(Ride ride) {
    return buildQuery(qRide)
      .select(qRide.rider.charity)
      .where(qRide.eq(ride))
      .fetchOne();
  }

  public User findDriverUser(Long rideId) {
    return buildQuery(qRide)
      .select(qRide.activeDriver.driver.user)
      .where(qRide.id.eq(rideId))
      .fetchOne();
  }

  @Transactional
  public void acceptRide(long id, ActiveDriver activeDriver) {
    queryFactory.update(qRide)
      .set(
        Arrays.asList(qRide.activeDriver, qRide.status),
        Arrays.asList(activeDriver, RideStatus.DRIVER_ASSIGNED)
      )
      .where(qRide.id.eq(id))
      .execute();
  }

  @Transactional
  public void setStatus(long id, RideStatus status) {
    queryFactory.update(qRide)
      .set(qRide.status, status)
      .where(qRide.id.eq(id))
      .execute();
  }

  public Long getRiderId(Ride ride) {
    return buildQuery(qRide)
      .select(qRide.rider.id)
      .where(qRide.eq(ride))
      .fetchOne();
  }

  public MobileDriverRideDto findOneForDriver(long id) {
    return findDriverRide(qRide.id.eq(id), getMobileDriverRideConstructor(), AbstractJPAQuery::fetchOne);
  }

  public MobileDriverRideEventPayloadDto findOneForDriverEvent(long id) {
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    return findDriverRide(qRide.id.eq(id), new QMobileDriverRideEventPayloadDto(qRide.id, qDriver.id, qRide.status,
      qRide.rider.id, qRide.rider.user.photoUrl, qRiderOverride.firstName.coalesce(qRide.rider.user.firstname),
      qRiderOverride.lastName.coalesce(qRide.rider.user.lastname), qRiderOverride.phoneNumber.coalesce(qRide.rider.user.phoneNumber),
      qRide.rider.user.email, qRide.rider.rating, qRide.startLocationLat, qRide.startLocationLong,
      qRide.endLocationLat, qRide.endLocationLong, qRide.start.address, qRide.end.address, qRide.start, qRide.end, qRide.surgeFactor,
      qRide.fareDetails.driverPayment, qRide.requestedCarType.title, qRide.requestedCarType.carCategory, qRide.requestedCarType.plainIconUrl,
      qRide.requestedCarType.configuration, qRide.requestedDriverTypeBitmask, qRide.comment, qRide.fareDetails.freeCreditCharged,
      qRide.rideMap, qRide.fareDetails.cancellationFee), AbstractJPAQuery::fetchOne);
  }

  public List<MobileDriverRideDto> findCurrentForDriver(ActiveDriver activeDriver) {
    return findDriverRide(
      qRide.status.in(RideStatus.ONGOING_DRIVER_STATUSES)
        .and(qRide.activeDriver.eq(activeDriver)),
      getMobileDriverRideConstructor(), AbstractJPAQuery::fetch
    );
  }

  private QMobileDriverRideDto getMobileDriverRideConstructor() {
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    return new QMobileDriverRideDto(qRide.id, qDriver.id, qRide.status,
      qRide.rider.id, qRide.rider.user.photoUrl, qRiderOverride.firstName.coalesce(qRide.rider.user.firstname),
      qRiderOverride.lastName.coalesce(qRide.rider.user.lastname), qRiderOverride.phoneNumber.coalesce(qRide.rider.user.phoneNumber),
      qRide.rider.user.email, qRide.rider.rating, qRide.startLocationLat, qRide.startLocationLong,
      qRide.endLocationLat, qRide.endLocationLong, qRide.start.address, qRide.end.address, qRide.start, qRide.end,
      qRide.surgeFactor, qRide.fareDetails.driverPayment, qRide.requestedCarType.title,
      qRide.requestedCarType.carCategory, qRide.requestedCarType.plainIconUrl,
      qRide.requestedCarType.configuration, qRide.requestedDriverTypeBitmask, qRide.comment, qRide.fareDetails.freeCreditCharged,
      qRide.rideMap);
  }

  private <T, R> T findDriverRide(BooleanExpression predicate, ConstructorExpression<R> fields, Function<JPAQuery<R>, T> consumer) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    JPAQuery<R> query = queryFactory.from(qRide)
      .select(fields)
      .leftJoin(qRide.activeDriver, qActiveDriver)
      .leftJoin(qRide.activeDriver.driver, qDriver)
      .leftJoin(qRide.riderOverride, qRiderOverride)
      .where(predicate);
    return consumer.apply(query);
  }

  public RideRequestContext getRequestContext(Ride ride) {
    return queryFactory.from(qRide)
      .select(new QRideRequestContext(qRide.id, qRide.rider.id, qRide.startLocationLat,
        qRide.startLocationLong, qRide.cityId, qRide.requestedCarType.carCategory, qRide.requestedCarType.bitmask,
        qRide.requestedDriverTypeBitmask, qRide.requestedOn, qRide.applePayToken))
      .where(qRide.eq(ride))
      .fetchOne();
  }

  public DispatchCandidate findDispatchCandidate(long id) {
    return queryFactory.from(qRide)
      .select(new QDispatchCandidate(qRide.activeDriver.id, qRide.activeDriver.driver.id, qRide.activeDriver.selectedCar.license,
        qRide.activeDriver.driver.user.id, qRide.activeDriver.status))
      .where(qRide.id.eq(id))
      .fetchOne();
  }

  public ETACalculationInfo getETACalculationInfo(long rideId) {
    return buildQuery(qRide)
      .select(new QETACalculationInfo(qRide.activeDriver.id, qRide.startLocationLat, qRide.startLocationLong))
      .where(qRide.id.eq(rideId))
      .fetchOne();
  }

  public ETCCalculationInfo getETCCalculationInfo(long rideId) {
    return buildQuery(qRide)
      .select(new QETCCalculationInfo(qRide.activeDriver.id, qRide.id, qRide.status, qRide.endLocationLat, qRide.endLocationLong))
      .where(qRide.id.eq(rideId))
      .fetchOne();
  }

  public ETCCalculationInfo getETCCalculationInfoForDriver(long activeDriverId) {
    return buildQuery(qRide)
      .select(new QETCCalculationInfo(qRide.activeDriver.id, qRide.id, qRide.status, qRide.endLocationLat, qRide.endLocationLong))
      .where(
        qRide.activeDriver.id.eq(activeDriverId),
        qRide.status.in(RideStatus.ONGOING_DRIVER_STATUSES)
      )
      .fetchFirst();
  }

  public MobileRiderRideDto findRiderRideInfo(long id) {
    BooleanBuilder booleanBuilder = new BooleanBuilder()
      .and(qRide.id.eq(id));
    return findRiderRide(booleanBuilder);
  }

  public MobileRiderRideDto findLastUnratedRide(User user) {
    BooleanBuilder booleanBuilder = new BooleanBuilder()
      .and(qRide.rider.user.eq(user))
      .and(qRide.status.eq(RideStatus.COMPLETED))
      .and(qRide.driverRating.isNull());
    return findRiderRide(booleanBuilder);
  }

  public DispatcherAccountRideDto findDispatcherRideInfo(long id) {
    BooleanBuilder booleanBuilder = new BooleanBuilder()
      .and(qRide.id.eq(id));
    final List<DispatcherAccountRideDto> result = findDispatcherRides(booleanBuilder);
    return result.isEmpty() ? null : result.get(0);
  }

  public List<DispatcherAccountRideDto> findLastUnratedRides(User user) {
    BooleanBuilder booleanBuilder = new BooleanBuilder()
      .and(qRide.rider.user.eq(user))
      .and(qRide.status.eq(RideStatus.COMPLETED))
      .and(qRide.driverRating.isNull())
      .and(qRide.createdDate.after(Date.from(Instant.now().minus(24, ChronoUnit.HOURS))));
    return findDispatcherRides(booleanBuilder);
  }

  public MobileRiderRideDto findOngoingRideForRider(User riderUser) {
    BooleanBuilder booleanBuilder = new BooleanBuilder()
      .and(qRide.rider.user.eq(riderUser))
      .and(qRide.status.in(RideStatus.ONGOING_RIDER_STATUSES));
    return findRiderRide(booleanBuilder);
  }

  public List<DispatcherAccountRideDto> findOngoingRidesForDispatcher(User user) {
    BooleanBuilder booleanBuilder = new BooleanBuilder()
      .and(qRide.rider.user.eq(user))
      .and(qRide.status.in(RideStatus.ONGOING_RIDER_STATUSES));
    return findDispatcherRides(booleanBuilder);
  }

  private List<DispatcherAccountRideDto> findDispatcherRides(final BooleanBuilder where) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    QUser qUser = QUser.user;
    QCar qCar = QCar.car;
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    return buildQuery(qRide)
      .leftJoin(qRide.activeDriver, qActiveDriver)
      .leftJoin(qActiveDriver.driver, qDriver)
      .leftJoin(qDriver.user, qUser)
      .leftJoin(qActiveDriver.selectedCar, qCar)
      .leftJoin(qRide.riderOverride, qRiderOverride)
      .select(new QDispatcherAccountRideDto(qRide.id, qRide.rider.id, qRide.status, qRide.driverAcceptedOn, qRide.completedOn,
        qRide.fareDetails.tip, qRide.driverRating, qRide.fareDetails.estimatedFare, qRide.startLocationLat, qRide.startLocationLong, qRide.endLocationLat,
        qRide.endLocationLong, qRide.start.address, qRide.end.address, qRide.start, qRide.end, qRide.fareDetails.totalFare,
        qRide.fareDetails.driverPayment, qRide.requestedCarType.carCategory, qRide.comment, qRide.fareDetails.freeCreditCharged,
        qRide.rideMap, qRide.cityId, qRide.fareDetails.roundUpAmount, qActiveDriver.id, qDriver.id, qDriver.rating,
        qDriver.grantedDriverTypesBitmask, qUser.id, qUser.email, qUser.firstname, qUser.lastname, qUser.phoneNumber, qDriver.active,
        qCar.id, qCar.color, qCar.license, qCar.make, qCar.model, qCar.year, qCar.carCategoriesBitmask,
        qRiderOverride.firstName, qRiderOverride.lastName, qRiderOverride.phoneNumber))
      .where(where)
      .orderBy(qRide.id.desc())
      .fetch();
  }

  private MobileRiderRideDto findRiderRide(BooleanBuilder predicate) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    QUser qUser = QUser.user;
    QCar qCar = QCar.car;

    return buildQuery(qRide)
      .leftJoin(qRide.activeDriver, qActiveDriver)
      .leftJoin(qActiveDriver.driver, qDriver)
      .leftJoin(qDriver.user, qUser)
      .leftJoin(qActiveDriver.selectedCar, qCar)
      .select(new QMobileRiderRideDto(qRide.id, qRide.rider.id, qRide.status, qRide.driverAcceptedOn, qRide.completedOn,
        qRide.fareDetails.tip, qRide.driverRating, qRide.fareDetails.estimatedFare, qRide.startLocationLat, qRide.startLocationLong, qRide.endLocationLat,
        qRide.endLocationLong, qRide.start.address, qRide.end.address, qRide.start, qRide.end, qRide.fareDetails.totalFare,
        qRide.fareDetails.driverPayment, qRide.requestedCarType.carCategory, qRide.comment, qRide.fareDetails.freeCreditCharged,
        qRide.rideMap, qRide.cityId, qRide.fareDetails.roundUpAmount, qActiveDriver.id, qDriver.id, qDriver.rating, qDriver.grantedDriverTypesBitmask,
        qUser.id, qUser.email, qUser.firstname, qUser.lastname, qUser.phoneNumber, qDriver.active, qCar.id, qCar.color, qCar.license,
        qCar.make, qCar.model, qCar.year, qCar.carCategoriesBitmask))
      .where(predicate)
      .orderBy(qRide.id.desc())
      .fetchFirst();
  }

  public PrecedingRide findPrecedingRide(long activeDriverId) {
    return buildQuery(qRide)
      .select(new QMobileRiderRideDto_PrecedingRide(qRide.id, qRide.status, qRide.end.address, qRide.end.zipCode,
        qRide.endLocationLat, qRide.endLocationLong))
      .where(
        qRide.activeDriver.id.eq(activeDriverId),
        qRide.status.eq(RideStatus.ACTIVE)
      )
      .fetchOne();
  }

  public MobileDriverRideDto findNextRide(long activeDriverId) {
    return findDriverRide(qRide.activeDriver.id.eq(activeDriverId).and(
      qRide.status.eq(RideStatus.DRIVER_ASSIGNED)), getMobileDriverRideConstructor(), AbstractJPAQuery::fetchOne);
  }

  public ActiveDriverDto getActiveDriverForRider(long id) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    return buildQuery(qActiveDriver)
      .select(new QMobileRiderRideDto_ActiveDriverDto(qActiveDriver.id, qActiveDriver.driver.id,
        qActiveDriver.driver.rating, qActiveDriver.driver.grantedDriverTypesBitmask, qActiveDriver.driver.user.id,
        qActiveDriver.driver.user.email, qActiveDriver.driver.user.firstname, qActiveDriver.driver.user.lastname,
        qActiveDriver.driver.user.nickName, qActiveDriver.driver.user.phoneNumber, qActiveDriver.driver.active,
        qActiveDriver.selectedCar.id, qActiveDriver.selectedCar.color, qActiveDriver.selectedCar.license,
        qActiveDriver.selectedCar.make, qActiveDriver.selectedCar.model, qActiveDriver.selectedCar.year,
        qActiveDriver.selectedCar.carCategoriesBitmask))
      .where(qActiveDriver.id.eq(id))
      .fetchOne();
  }

  public List<DirectConnectHistoryDto> getDirectConnectHistoryForRider(Long rider) {
    return queryFactory.from(qRide)
      .select(new QDirectConnectHistoryDto(qRide.activeDriver.driver.id, qRide.activeDriver.driver.directConnectId,
        qRide.activeDriver.driver.user.firstname, qRide.activeDriver.driver.user.lastname, qRide.requestedOn)
      )
      .where(
        qRide.rider.id.eq(rider),
        qRide.status.eq(RideStatus.COMPLETED),
        qRide.requestedDriverTypeBitmask.eq(DriverType.DIRECT_CONNECT_BITMASK)
      )
      .fetch();
  }

  @Transactional
  public void setPrechargeId(Long rideId, String preChargeId) {
    queryFactory.update(qRide)
      .set(qRide.preChargeId, preChargeId)
      .where(qRide.id.eq(rideId))
      .execute();
  }

  @Transactional
  public void cancelRide(Long rideId, RideStatus status, FareDetails fareDetails, PaymentStatus paymentStatus) {
    queryFactory.update(qRide)
      .set(
        Arrays.asList(qRide.status, qRide.fareDetails.airportFee, qRide.fareDetails.baseFare, qRide.fareDetails.bookingFee,
          qRide.fareDetails.cancellationFee, qRide.fareDetails.cityFee, qRide.fareDetails.distanceFare, qRide.fareDetails.driverPayment,
          qRide.fareDetails.estimatedFare, qRide.fareDetails.freeCreditCharged, qRide.fareDetails.minimumFare, qRide.fareDetails.normalFare,
          qRide.fareDetails.processingFee, qRide.fareDetails.raPayment, qRide.fareDetails.ratePerMile, qRide.fareDetails.ratePerMinute,
          qRide.fareDetails.roundUpAmount, qRide.fareDetails.stripeCreditCharge, qRide.fareDetails.subTotal, qRide.fareDetails.surgeFare,
          qRide.fareDetails.timeFare, qRide.fareDetails.tip, qRide.fareDetails.totalFare, qRide.cancelledOn, qRide.paymentStatus),
        Arrays.asList(status, fareDetails.getAirportFee(), fareDetails.getBaseFare(), fareDetails.getBookingFee(),
          fareDetails.getCancellationFee(), fareDetails.getCityFee(), fareDetails.getDistanceFare(), fareDetails.getDriverPayment(),
          fareDetails.getEstimatedFare(), fareDetails.getFreeCreditCharged(), fareDetails.getMinimumFare(), fareDetails.getNormalFare(),
          fareDetails.getProcessingFee(), fareDetails.getRaPayment(), fareDetails.getRatePerMile(), fareDetails.getRatePerMinute(),
          fareDetails.getRoundUpAmount(), fareDetails.getStripeCreditCharge(), fareDetails.getSubTotal(), fareDetails.getSurgeFare(),
          fareDetails.getTimeFare(), fareDetails.getTip(), fareDetails.getTotalFare(), new Date(), paymentStatus)
      )
      .where(qRide.id.eq(rideId))
      .execute();
  }
}