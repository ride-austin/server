package com.rideaustin.driverstatistic.jobs;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rideaustin.driverstatistic.model.DriverId;
import com.rideaustin.driverstatistic.model.DriverStatistic;
import com.rideaustin.driverstatistic.model.DriverStatisticRepository;
import com.rideaustin.jobs.BaseJob;

import lombok.Setter;

@Component
public class DriverStatisticJob extends BaseJob {

  private static final String SELECT_ID_FROM_DRIVERS = "SELECT id FROM drivers";

  private static final String MIN_DATE_AND_COUNT_ACCEPTED_QUERY = "select min(created_date), count(*) from (" +
    "select rdd.created_date from ride_driver_dispatches rdd " +
    "inner join active_drivers ad on rdd.active_driver_id = ad.id " +
    "where ad.driver_id=? " +
    "order by rdd.id desc " +
    "limit 100) d";

  private static final String MIN_DATE_AND_COUNT_CANCELLED_QUERY = "select min(created_date), count(*) from (" +
    "select r.created_date from rides r " +
    "inner join active_drivers ad on r.active_driver_id = ad.id " +
    "where ad.driver_id=? " +
    "order by r.id desc " +
    "limit 100) d";

  private static final String ACCEPTED_QUERY = "select count(r.id) c from rides r  " +
    "inner join active_drivers ad on ad.id = r.active_driver_id " +
    "where ad.driver_id=? and r.created_date > ?";

  private static final String CANCELLED_QUERY = "select count(r.id) c from rides r  " +
    "inner join active_drivers ad on ad.id = r.active_driver_id " +
    "where ad.driver_id=? and r.created_date > ? and r.status='DRIVER_CANCELLED'";

  @Setter(onMethod = @__(@Inject))
  private JdbcTemplate jdbcTemplate;

  @Setter(onMethod = @__(@Inject))
  private DriverStatisticRepository repository;

  @Override
  protected void executeInternal() {
    List<Long> driverIds = jdbcTemplate.queryForList(SELECT_ID_FROM_DRIVERS, Long.class);
    List<DriverStatistic> statistics = new ArrayList<>();

    for (Long driverId : driverIds) {
      Pair<Timestamp, Integer> acceptedMinDateAndCount = jdbcTemplate.queryForObject(
        MIN_DATE_AND_COUNT_ACCEPTED_QUERY,
        new Object[]{driverId},
        (rs, rowNum) -> ImmutablePair.of(rs.getTimestamp(1), rs.getInt(2)));

      Timestamp acceptedMinDate = acceptedMinDateAndCount.getLeft();

      Pair<Timestamp, Integer> cancelledMinDateAndCount = jdbcTemplate.queryForObject(
        MIN_DATE_AND_COUNT_CANCELLED_QUERY,
        new Object[]{driverId},
        (rs, rowNum) -> ImmutablePair.of(rs.getTimestamp(1), rs.getInt(2)));

      Timestamp cancelledMinDate = cancelledMinDateAndCount.getLeft();

      Integer accepted = jdbcTemplate.queryForObject(ACCEPTED_QUERY,
        new Object[]{driverId, acceptedMinDate},
        Integer.class);
      Integer cancelled = jdbcTemplate.queryForObject(CANCELLED_QUERY,
        new Object[]{driverId, cancelledMinDate},
        Integer.class);

      DriverStatistic statsItem = DriverStatistic.findOrCreate(new DriverId(driverId));
      statsItem.setLastAcceptedCount(accepted);
      statsItem.setLastAcceptedOver(acceptedMinDateAndCount.getRight());
      statsItem.setLastCancelledCount(cancelled);
      statsItem.setLastCancelledOver(cancelledMinDateAndCount.getRight());
      statistics.add(statsItem);
    }

    repository.save(statistics);
  }

  @Override
  protected String getDescription() {
    return "Count last accepted cancelled";
  }
}
