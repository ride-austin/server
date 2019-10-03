package com.rideaustin.repo.dsl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.City;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.promocodes.PromocodeType;
import com.rideaustin.model.promocodes.QPromocodeRedemption;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.PromocodeRedemptionDTO;
import com.rideaustin.rest.model.QPromocodeRedemptionDTO;

@Repository
public class PromocodeRedemptionDslRepository extends AbstractDslRepository {

  private static QPromocodeRedemption qPromocodeRedemption = QPromocodeRedemption.promocodeRedemption;

  public PromocodeRedemption findPromocodeRedemption(Promocode promocode, Rider rider) {
    return buildQuery(qPromocodeRedemption)
      .where(qPromocodeRedemption.rider.eq(rider)
        .and(qPromocodeRedemption.promocode.eq(promocode)))
      .fetchOne();

  }

  public List<PromocodeRedemption> findActiveRedemptions(Long riderId) {
    return buildQuery(qPromocodeRedemption)
      .where(qPromocodeRedemption.rider.id.eq(riderId)
        .and(qPromocodeRedemption.active.isTrue()))
      .fetch();
  }

  public List<PromocodeRedemption> findExpiredRedemptions(Date currentDate) {
    return buildQuery(qPromocodeRedemption)
      .where(qPromocodeRedemption.active.isTrue()
        .and(qPromocodeRedemption.validUntil.before(currentDate)))
      .fetch();
  }

  public Long countReferralRedemptions(Rider rider) {
    return buildQuery(qPromocodeRedemption)
      .where(qPromocodeRedemption.rider.eq(rider)
        .and(qPromocodeRedemption.promocode.promocodeType.eq(PromocodeType.USER)))
      .fetchCount();
  }

  public PromocodeRedemption findOne(Long id) {
    return get(id, PromocodeRedemption.class);
  }

  public BigDecimal getRemainingSumForRider(Long riderId, City city) {
    Integer bitmask = city.getBitmask();
    return buildQuery(qPromocodeRedemption)
      .where(
        qPromocodeRedemption.active.isTrue(),
        qPromocodeRedemption.rider.id.eq(riderId),
        bitmaskPredicate(qPromocodeRedemption.promocode.cityBitmask, bitmask),
        qPromocodeRedemption.validUntil.isNull().or(qPromocodeRedemption.validUntil.after(new Date()))
      )
      .select(qPromocodeRedemption.remainingValue.sum())
      .fetchOne();
  }

  public List<PromocodeRedemptionDTO> getRedemptionsInfo(Long riderId, City city) {
    Integer bitmask = city.getBitmask();
    return queryFactory.select(
      new QPromocodeRedemptionDTO(
        qPromocodeRedemption.promocode.codeLiteral, qPromocodeRedemption.promocode.codeValue,
        qPromocodeRedemption.createdDate, qPromocodeRedemption.validUntil,
        qPromocodeRedemption.remainingValue, qPromocodeRedemption.numberOfTimesUsed,
        qPromocodeRedemption.promocode.maximumUsesPerAccount)
    )
      .from(qPromocodeRedemption)
      .where(
        qPromocodeRedemption.rider.id.eq(riderId),
        qPromocodeRedemption.active.isTrue(),
        bitmaskPredicate(qPromocodeRedemption.promocode.cityBitmask, bitmask),
        qPromocodeRedemption.validUntil.isNull().or(qPromocodeRedemption.validUntil.after(new Date()))
      )
      .orderBy(qPromocodeRedemption.validUntil.desc())
      .fetch();
  }

  public List<PromocodeRedemption> findNewRiderRedemptions(Long riderId, PromocodeRedemption promocodeRedemption) {
    return buildQuery(qPromocodeRedemption)
      .where(
        qPromocodeRedemption.rider.id.eq(riderId),
        qPromocodeRedemption.promocode.newRidersOnly.isTrue(),
        qPromocodeRedemption.ne(promocodeRedemption)
      )
      .fetch();
  }
}
