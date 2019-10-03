package com.rideaustin.repo.dsl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.QBlockedDevice;
import com.rideaustin.model.user.Rider;

@Repository
public class BlockedDeviceDslRepository extends AbstractDslRepository {

  private static final QBlockedDevice qBlockedDevice = QBlockedDevice.blockedDevice;

  public boolean isBlocked(String deviceId) {
    return !buildQuery(qBlockedDevice)
      .where(qBlockedDevice.deviceId.eq(deviceId))
      .fetch()
      .isEmpty();
  }

  public boolean isBlocked(Long userId) {
    return !buildQuery(qBlockedDevice)
      .where(qBlockedDevice.rider.user.id.eq(userId))
      .fetch()
      .isEmpty();
  }

  public List<String> unblock(Rider rider) {
    List<String> ids = buildQuery(qBlockedDevice)
      .select(qBlockedDevice.deviceId)
      .where(qBlockedDevice.rider.eq(rider))
      .fetch();
    queryFactory.delete(qBlockedDevice)
      .where(qBlockedDevice.rider.eq(rider))
      .execute();
    return ids;
  }

  public Collection<String> findAllIds() {
    return buildQuery(qBlockedDevice)
      .select(qBlockedDevice.deviceId)
      .distinct()
      .fetch();
  }
}
