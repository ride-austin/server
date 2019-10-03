package com.rideaustin.repo.dsl;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.QRider;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.model.ListRidersParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.QRiderDto;
import com.rideaustin.rest.model.QSimpleRiderDto;
import com.rideaustin.rest.model.RiderDto;
import com.rideaustin.rest.model.SimpleRiderDto;

import lombok.NonNull;

@Repository
public class RiderDslRepository extends AbstractDslRepository {

  private static final String SELECT_RATING_AVERAGE = "SELECT calculate_rating_average(:defaultRating, :minimumRatingThreshold, :limit, :id) from DUAL ";
  private static final QRider qRider = QRider.rider;
  private static final QRide qRide = QRide.ride;

  public List<Rider> findByUserWithDependencies(User user) {
    return buildQuery(qRider).leftJoin(qRider.charity).fetchJoin().where(qRider.user.eq(user)).fetch();
  }

  public RiderDto findRiderInfo(long riderId) {
    return queryFactory.from(qRider)
      .select(new QRiderDto(qRider.id, qRider.user.id, qRider.user.photoUrl, qRider.user.email, qRider.user.facebookId,
        qRider.user.firstname, qRider.user.middleName, qRider.user.lastname, qRider.user.nickName, qRider.user.phoneNumber,
        qRider.active, qRider.user.userEnabled, qRider.user.gender, qRider.user.address, qRider.user.dateOfBirth,
        qRider.primaryCard.cardExpired, qRider.dispatcherAccount, qRider.rating, qRider.charity.id, qRider.charity.name,
        qRider.charity.description, qRider.charity.imageUrl))
      .leftJoin(qRider.charity).fetchJoin()
      .where(qRider.id.eq(riderId))
      .fetchOne();
  }

  public RiderDto findRiderByUser(User user) {
    return queryFactory.from(qRider)
      .select(new QRiderDto(qRider.id, qRider.user.id, qRider.user.photoUrl, qRider.user.email, qRider.user.facebookId,
        qRider.user.firstname, qRider.user.middleName, qRider.user.lastname, qRider.user.nickName, qRider.user.phoneNumber,
        qRider.active, qRider.user.userEnabled, qRider.user.gender, qRider.user.address, qRider.user.dateOfBirth,
        qRider.primaryCard.cardExpired, qRider.dispatcherAccount, qRider.rating, qRider.charity.id, qRider.charity.name,
        qRider.charity.description, qRider.charity.imageUrl))
      .leftJoin(qRider.charity).fetchJoin()
      .where(qRider.user.eq(user))
      .fetchOne();
  }

  public Rider getRiderWithDependencies(Long id) {
    return buildQuery(qRider)
      .leftJoin(qRider.charity).fetchJoin()
      .where(qRider.id.eq(id)).fetchOne();
  }

  public Page<Rider> findRiders(ListRidersParams params, PagingParams paging) {
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);
    return getPage(paging, builder, qRider);
  }

  public Page<SimpleRiderDto> findRidersDto(ListRidersParams params, PagingParams paging) {
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);

    JPAQuery query = queryFactory.select(new QSimpleRiderDto(qRider.user.id, qRider.user.firstname, qRider.user.lastname, qRider.user.email,
      qRider.user.phoneNumber, qRider.active, qRider.user.userEnabled, qRider.id, qRider.user.photoUrl
    )).from(qRider).where(builder);

    long total = query.fetchCount();
    List<SimpleRiderDto> content = appendPagingParams(query, paging, qRider).fetch();

    return new PageImpl<>(content, paging.toPageRequest(), total);
  }

  public Rider findByPhoneNumber(String phoneNumber, boolean useEndsWith) throws NotFoundException {
    JPAQuery<Rider> jpaQuery = buildQuery(qRider);
    if (useEndsWith) {
      jpaQuery.where(qRider.user.phoneNumber.endsWith(phoneNumber));
    } else {
      jpaQuery.where(qRider.user.phoneNumber.eq(phoneNumber));
    }
    List<Rider> riders = jpaQuery.fetch();
    if (CollectionUtils.isEmpty(riders)) {
      return null;
    }
    if (riders.size() > 1) {
      throw new NotFoundException("Multiple riders found for provided phone number");
    }
    return riders.get(0);

  }

  public Long getRiderRatingCount(Rider rider) {
    return queryFactory.select(QRide.ride.riderRating.count()).from(qRide)
      .where(qRide.rider.eq(rider).and(qRide.riderRating.isNotNull())).fetchOne();
  }

  public Rider getRider(Long id) {
    return get(id, Rider.class);
  }

  public Ride getActiveRide(@Nonnull Rider rider) {
    return buildQuery(qRide)
      .where(qRide.rider.eq(rider)
        .and(qRide.status.in(RideStatus.ONGOING_RIDER_STATUSES))).fetchOne();
  }

  public Double findRatingAverage(@NonNull Double defaultRating, @NonNull Integer minimumRatingThreshold, @NonNull Integer limit, @NonNull Rider rider) {
    Query query = this.entityManager.createNativeQuery(SELECT_RATING_AVERAGE);
    query.setParameter("defaultRating", defaultRating);
    query.setParameter("minimumRatingThreshold", minimumRatingThreshold);
    query.setParameter("limit", limit);
    query.setParameter("id", rider.getId());
    return ((BigDecimal) query.getSingleResult()).doubleValue();
  }
}
