package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.rideaustin.model.QLostAndFoundRequest;
import com.rideaustin.model.lostandfound.LostAndFoundRequestDto;
import com.rideaustin.model.lostandfound.LostItemInfo;
import com.rideaustin.model.lostandfound.QLostAndFoundRequestDto;
import com.rideaustin.model.ride.QRide;

@Repository
public class LostAndFoundDslRepository extends AbstractDslRepository {

  private static final QRide qRide = QRide.ride;
  private static final QLostAndFoundRequest qRequest = QLostAndFoundRequest.lostAndFoundRequest;

  public LostItemInfo getLostItemInfo(Long rideId) {
    return buildQuery(qRide)
      .select(
        Projections.bean(
          LostItemInfo.class, qRide.id.as("rideId"), qRide.rider.user.id.as("riderUserId"), qRide.rider.id.as("riderId"),
          qRide.rider.user.email.as("riderEmail"), qRide.rider.user.firstname.as("riderFirstName"),
          qRide.rider.user.lastname.as("riderLastName"), qRide.rider.user.phoneNumber.as("riderPhone"),
          qRide.activeDriver.driver.user.email.as("driverEmail"), qRide.activeDriver.driver.user.phoneNumber.as("driverPhone"),
          qRide.activeDriver.driver.user.firstname.as("driverFirstName"),
          qRide.activeDriver.driver.user.lastname.as("driverLastName"), qRide.cityId
        )
      )
      .where(qRide.id.eq(rideId))
      .fetchOne();
  }

  public List<LostAndFoundRequestDto> findRequests(Long avatarId) {
    return queryFactory.select(
      new QLostAndFoundRequestDto(qRequest.type, qRequest.content, qRequest.createdDate)
    ).from(qRequest)
      .where(qRequest.requestedBy.eq(avatarId))
      .orderBy(qRequest.id.desc())
      .fetch();
  }
}
