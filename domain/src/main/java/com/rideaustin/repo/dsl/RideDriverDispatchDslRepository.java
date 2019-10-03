package com.rideaustin.repo.dsl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.ride.QActiveDriver;
import com.rideaustin.model.ride.QRideDriverDispatch;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideDriverDispatch;
import com.rideaustin.model.user.QDriver;
import com.rideaustin.service.model.DispatchRequest;
import com.rideaustin.service.model.QDispatchRequest;

@Repository
public class RideDriverDispatchDslRepository extends AbstractDslRepository {

  private static final QRideDriverDispatch qRideDriverDispatch = QRideDriverDispatch.rideDriverDispatch;

  public RideDriverDispatch findByRideAndStatus(Long rideId, DispatchStatus dispatchStatus) {
    return buildQuery(qRideDriverDispatch)
      .where(
        qRideDriverDispatch.ride.id.eq(rideId),
        qRideDriverDispatch.status.eq(dispatchStatus)
      )
      .orderBy(qRideDriverDispatch.id.asc())
      .fetchFirst();
  }

  public List<DispatchRequest> findDispatchedRequests(Ride ride) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    QDriver qDriver = QDriver.driver;
    return queryFactory.from(qRideDriverDispatch)
      .innerJoin(qRideDriverDispatch.activeDriver, qActiveDriver)
      .innerJoin(qActiveDriver.driver, qDriver)
      .select(new QDispatchRequest(qRideDriverDispatch.id, qActiveDriver.id, qDriver.id))
      .where(
        qRideDriverDispatch.ride.eq(ride),
        qRideDriverDispatch.status.eq(DispatchStatus.DISPATCHED)
      )
      .fetch();
  }

  @Transactional
  public void missRequests(Collection<Long> ids) {
    queryFactory
      .update(qRideDriverDispatch)
      .set(qRideDriverDispatch.status, DispatchStatus.MISSED)
      .where(qRideDriverDispatch.id.in(ids))
      .execute();
  }

  @Transactional
  public void declineRequest(Long rideId, Long activeDriverId, DispatchStatus dispatchStatus) {
    queryFactory
      .update(qRideDriverDispatch)
      .set(qRideDriverDispatch.status, dispatchStatus)
      .where(
        qRideDriverDispatch.ride.id.eq(rideId),
        qRideDriverDispatch.activeDriver.id.eq(activeDriverId)
      )
      .execute();
  }

  public DispatchRequest findLastMissedRequestByRide(Long rideId) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    QDriver qDriver = QDriver.driver;
    return queryFactory
      .from(qRideDriverDispatch)
      .innerJoin(qRideDriverDispatch.activeDriver, qActiveDriver)
      .innerJoin(qActiveDriver.driver, qDriver)
      .select(new QDispatchRequest(qRideDriverDispatch.id, qActiveDriver.id, qDriver.id))
      .where(
        qRideDriverDispatch.ride.id.eq(rideId),
        qRideDriverDispatch.status.eq(DispatchStatus.MISSED)
      )
      .orderBy(qRideDriverDispatch.id.desc())
      .fetchFirst();
  }

  public RideDriverDispatch findByRideAndActiveDriverAndStatus(Long rideId, DispatchStatus dispatchStatus, Long activeDriverId) {
    return buildQuery(qRideDriverDispatch)
      .where(qRideDriverDispatch.ride.id.eq(rideId).and(qRideDriverDispatch.status.eq(dispatchStatus).and(qRideDriverDispatch.activeDriver.id.eq(activeDriverId))))
      .fetchOne();
  }

  public RideDriverDispatch findByRideAndActiveDriver(Long rideId, Long activeDriverId) {
    return buildQuery(qRideDriverDispatch)
      .where(qRideDriverDispatch.ride.id.eq(rideId).and(qRideDriverDispatch.activeDriver.id.eq(activeDriverId)))
      .fetchOne();
  }

}
