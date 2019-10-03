package com.rideaustin.repo.dsl;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.enums.SplitFareStatus;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.ride.QRiderOverride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.splitfare.QFarePayment;
import com.rideaustin.model.user.QRider;
import com.rideaustin.model.user.QRiderCard;
import com.rideaustin.rest.model.FarePaymentDto;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.QFarePaymentDto;
import com.rideaustin.rest.model.QSplitFareDto;
import com.rideaustin.rest.model.SplitFareDto;
import com.rideaustin.service.model.PendingPaymentDto;
import com.rideaustin.service.model.QPendingPaymentDto;

@Repository
public class FarePaymentDslRepository extends AbstractDslRepository {

  private static final QFarePayment qFarePayment = QFarePayment.farePayment;

  public FarePayment findFarePayment(Long rideId, Long riderId) {
    return buildQuery(qFarePayment)
      .where(qFarePayment.ride.id.eq(rideId)
        .and(qFarePayment.rider.id.eq(riderId)))
      .fetchOne();
  }

  public List<FarePayment> findAcceptedFarePayments(Long rideId) {
    return buildQuery(qFarePayment)
      .where(
        qFarePayment.ride.id.eq(rideId),
        qFarePayment.splitStatus.eq(SplitFareStatus.ACCEPTED)
      )
      .fetch();
  }

  public List<FarePaymentDto> findAcceptedFarePaymentInfo(Long rideId) {
    QRide qRide = QRide.ride;
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    return queryFactory.select(
      new QFarePaymentDto(qFarePayment.id, qFarePayment.ride.id, qFarePayment.rider.id,
        getFullName(qFarePayment.rider.user, qRiderOverride.firstName, qRiderOverride.lastName),
        qFarePayment.rider.user.photoUrl, qFarePayment.splitStatus, qFarePayment.createdDate, qFarePayment.updatedDate,
        qFarePayment.mainRider, qFarePayment.freeCreditCharged, qFarePayment.stripeCreditCharge, qFarePayment.usedCard.id,
        qFarePayment.usedCard.cardNumber, qFarePayment.usedCard.cardBrand, qFarePayment.usedCard.cardExpired,
        qFarePayment.usedCard.eq(qFarePayment.rider.primaryCard), qFarePayment.chargeId, qFarePayment.paymentStatus,
        qFarePayment.provider)
    )
      .from(qFarePayment)
      .leftJoin(qFarePayment.ride, qRide)
      .leftJoin(qRide.riderOverride, qRiderOverride)
      .where(
        qFarePayment.ride.id.eq(rideId),
        qFarePayment.splitStatus.eq(SplitFareStatus.ACCEPTED)
      )
      .fetch();
  }

  public List<SplitFareDto> findFarePayments(Long rideId) {
    QRide qRide = QRide.ride;
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    return createSelectQuery(qRide, qRiderOverride)
      .where(qRide.id.eq(rideId))
      .fetch();
  }

  public List<SplitFareDto> findPendingSplitFareRequestForRider(Long rider) {
    QRide qRide = QRide.ride;
    QRiderOverride qRiderOverride = QRiderOverride.riderOverride;
    return createSelectQuery(qRide, qRiderOverride)
      .where(
        qFarePayment.rider.id.eq(rider),
        qFarePayment.mainRider.isFalse(),
        qFarePayment.splitStatus.eq(SplitFareStatus.REQUESTED),
        qFarePayment.ride.status.in(RideStatus.ONGOING_RIDER_STATUSES)
      )
      .fetch();
  }

  public FarePayment findOne(Long id) {
    return get(id, FarePayment.class);
  }

  public Optional<FarePayment> findMainRiderFarePayment(Long rideId) {
    return Optional.ofNullable(
      buildQuery(qFarePayment)
        .where(
          qFarePayment.ride.id.eq(rideId),
          qFarePayment.mainRider.isTrue()
        )
        .fetchOne()
    );
  }

  public Page<FarePayment> getRidePaymentHistoryFarePayment(@Nonnull Long riderId, @Nullable RideStatus rideStatus, @Nonnull PagingParams paging) {
    QFarePayment farePayment = QFarePayment.farePayment;

    QRide qFareRide = QRide.ride;
    QRider qRider = QRider.rider;
    QRiderCard qRiderCard = QRiderCard.riderCard;

    BooleanExpression whereParams = qFarePayment.rider.id.eq(riderId)
      .and(qFarePayment.splitStatus.eq(SplitFareStatus.ACCEPTED))
      .andAnyOf(
        qFarePayment.freeCreditCharged.goe(Constants.ZERO_USD),
        qFarePayment.stripeCreditCharge.goe(Constants.ZERO_USD)
      );
    if (rideStatus != null) {
      whereParams = whereParams.and(qFarePayment.ride.status.eq(rideStatus));
    }

    JPAQuery<FarePayment> query = buildQuery(farePayment)
      .leftJoin(farePayment.ride, qFareRide)
      .leftJoin(farePayment.rider, qRider)
      .leftJoin(farePayment.usedCard, qRiderCard)
      .where(whereParams);

    long total = query.fetchCount();
    List<FarePayment> content = appendPagingParams(query, paging, farePayment).fetch();

    return new PageImpl<>(content, paging.toPageRequest(), total);
  }

  public List<PendingPaymentDto> listPendingPaymentsForRider(long riderId) {
    return buildQuery(qFarePayment)
      .select(new QPendingPaymentDto(qFarePayment.ride, qFarePayment.stripeCreditCharge, qFarePayment.chargeScheduled))
      .where(
        qFarePayment.rider.id.eq(riderId),
        qFarePayment.ride.paymentStatus.in(PaymentStatus.UNPAID, PaymentStatus.BLOCKED),
        qFarePayment.paymentStatus.in(PaymentStatus.UNPAID, PaymentStatus.BLOCKED)
      )
      .fetch();
  }

  private JPAQuery<SplitFareDto> createSelectQuery(QRide qRide, QRiderOverride qRiderOverride) {
    return queryFactory.select(
      new QSplitFareDto(qFarePayment.id, qFarePayment.ride.id, qFarePayment.rider.id,
        getFullName(qFarePayment.rider.user), qFarePayment.rider.user.photoUrl, qFarePayment.splitStatus,
        qFarePayment.createdDate, qFarePayment.updatedDate,
        getFullName(qRide.rider.user, qRiderOverride.firstName, qRiderOverride.lastName), qRide.rider.user.photoUrl)
    )
      .from(qFarePayment)
      .leftJoin(qFarePayment.ride, qRide)
      .leftJoin(qRide.riderOverride, qRiderOverride);
  }
}
