package com.rideaustin.repo.dsl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.promocodes.PromocodeType;
import com.rideaustin.model.promocodes.QPromocode;
import com.rideaustin.model.promocodes.QPromocodeRedemption;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.user.QRider;
import com.rideaustin.rest.model.ListPromocodeDto;
import com.rideaustin.rest.model.ListPromocodeParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.QListPromocodeDto;

@Repository
public class PromocodeDslRepository extends AbstractDslRepository {

  private static QPromocode qPromocode = QPromocode.promocode;
  private static QRider qRider = QRider.rider;

  public Promocode findOne(Long promocodeId) {

    BooleanBuilder builder = new BooleanBuilder();
    promocodeTypeCriteria(builder, PromocodeType.PUBLIC);
    builder.and(qPromocode.id.eq(promocodeId));
    return buildQuery(qPromocode)
      .where(builder).fetchOne();
  }

  public Promocode findPromocodeByRider(Long riderId) {

    BooleanBuilder builder = new BooleanBuilder();
    promocodeTypeCriteria(builder, PromocodeType.USER);
    builder.and(qPromocode.owner.id.eq(riderId));
    return buildQuery(qPromocode)
      .join(qPromocode.owner, qRider).fetchJoin()
      .where(builder).fetchOne();
  }

  public Promocode findByLiteral(String stringLiteral) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(qPromocode.codeLiteral.eq(stringLiteral));

    return buildQuery(qPromocode)
      .where(builder).fetchOne();
  }

  public Page<ListPromocodeDto> findPromocodes(ListPromocodeParams searchCriteria, PagingParams paging) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(qPromocode.promocodeType.eq(PromocodeType.PUBLIC));

    if (searchCriteria.getCityBitMask() != null) {
      builder.and(bitmaskPredicate(qPromocode.cityBitmask, searchCriteria.getCityBitMask()));
    }

    if (!StringUtils.isEmpty(searchCriteria.getCodeLiteral())) {
      builder.and(qPromocode.codeLiteral.like(String.format("%%%s%%", searchCriteria.getCodeLiteral())));
    }

    JPAQuery<ListPromocodeDto> query = queryFactory.from(qPromocode)
      .select(new QListPromocodeDto(qPromocode.id, qPromocode.title, qPromocode.codeLiteral, qPromocode.codeValue,
        qPromocode.startsOn, qPromocode.endsOn, qPromocode.newRidersOnly, qPromocode.maximumRedemption, qPromocode.currentRedemption,
        qPromocode.maximumUsesPerAccount, qPromocode.cityBitmask, qPromocode.carTypeBitmask, qPromocode.validForNumberOfRides,
        qPromocode.validForNumberOfDays, qPromocode.useEndDate, qPromocode.nextTripOnly, qPromocode.applicableToFees,
        qPromocode.cappedAmountPerUse))
      .where(builder);

    long total = query.fetchCount();
    List<ListPromocodeDto> content = appendPagingParams(query, paging, qPromocode).fetch();

    return new PageImpl<>(content, paging.toPageRequest(), total);
  }

  public Long findUsageCount(Long id) {
    QPromocodeRedemption qPromocodeRedemption = QPromocodeRedemption.promocodeRedemption;
    QRide qRide = QRide.ride;
    return queryFactory.from(qPromocodeRedemption)
      .innerJoin(qPromocodeRedemption.promocode, qPromocode)
      .leftJoin(qRide).on(qRide.promocodeRedemptionId.eq(qPromocodeRedemption.id))
      .select(qRide.id.count())
      .where(qPromocodeRedemption.promocode.id.eq(id))
      .fetchOne();
  }

  private void promocodeTypeCriteria(BooleanBuilder builder, PromocodeType promocodeType) {
    builder.and(qPromocode.promocodeType.eq(promocodeType));
  }

}
