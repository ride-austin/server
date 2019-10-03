package com.rideaustin.model;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.ride.QActiveDriver;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.user.QDriver;
import com.rideaustin.model.user.QUser;
import com.rideaustin.repo.dsl.AbstractDslRepository;

@Repository
public class RidePushNotificationRepository extends AbstractDslRepository {

  private static final QRide qRide = QRide.ride;

  public RidePushNotificationDTO get(long rideId) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    QDriver qDriver = QDriver.driver;
    QUser qUser = QUser.user;
    return queryFactory.from(qRide)
      .leftJoin(qRide.activeDriver, qActiveDriver)
      .leftJoin(qActiveDriver.driver, qDriver)
      .leftJoin(qDriver.user, qUser)
      .select(new QRidePushNotificationDTO(qRide.id,
        qUser.nickName.when("").then(qUser.firstname).otherwise(qUser.nickName.coalesce(qUser.firstname)),
        qRide.status, qRide.fareDetails.cancellationFee, qRide.rider.user).skipNulls())
      .where(qRide.id.eq(rideId))
      .fetchOne();
  }
}
