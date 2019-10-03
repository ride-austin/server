package com.rideaustin.repo.dsl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ActiveDriversRecoveryRepository extends AbstractDslRepository {

  private static final String STUCK_RIDING_CANDIDATES_QUERY = "SELECT ad.id " +
    "         FROM rides r " +
    "           INNER JOIN active_drivers ad ON r.active_driver_id = ad.id " +
    "         WHERE ad.status = 'RIDING' AND r.status IN " +
    "                                        ('REQUESTED', 'DRIVER_CANCELLED', 'RIDER_CANCELLED', 'COMPLETED', 'ADMIN_CANCELLED', 'NO_AVAILABLE_DRIVER') " +
    "               AND NOT exists(SELECT 1 " +
    "                              FROM rides r " +
    "                              WHERE r.active_driver_id = ad.id AND " +
    "                                    r.status IN ('ACTIVE', 'DRIVER_REACHED', 'DRIVER_ASSIGNED'))";

  private static final String UPDATE_STUCK_RIDING_DRIVERS_QUERY = "UPDATE active_drivers SET status = 'INACTIVE', inactive_on = now() WHERE id = ?";

  private final JdbcTemplate jdbcTemplate;

  @Modifying
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Retryable(value = {Exception.class}, backoff = @Backoff(5000))
  public int cleanUpRidingDrivers(List<Long> candidates) {
    int result = 0;
    for (Long candidate : candidates) {
      result += jdbcTemplate.update(UPDATE_STUCK_RIDING_DRIVERS_QUERY, candidate);
    }
    return result;
  }

  public List<Long> findRidingStuckCandidates() {
    return jdbcTemplate.queryForList(STUCK_RIDING_CANDIDATES_QUERY, Long.class);
  }
}
