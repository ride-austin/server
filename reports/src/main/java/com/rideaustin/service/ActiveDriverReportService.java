package com.rideaustin.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ActiveDriverReportService {

  private final ActiveDriverDslRepository activeDriverDslRepository;

  public long getDriverOnlineSeconds(Driver driver, Instant from, Instant to) {
    AtomicLong sum = new AtomicLong();
    activeDriverDslRepository.getActiveDrivers(driver, Date.from(from), Date.from(to))
      .forEach(ad -> {
        Instant onlineStart = DateUtils.dateToInstant(ad.getCreatedDate());
        if (onlineStart.isBefore(from)) {
          onlineStart = from;
        }
        Instant onlineEnd = DateUtils.dateToInstant(ad.getInactiveOn() != null ? ad.getInactiveOn() : ad.getUpdatedDate());
        if (onlineEnd.isAfter(to)) {
          onlineEnd = to;
        }
        sum.addAndGet(Duration.between(onlineStart, onlineEnd).get(ChronoUnit.SECONDS));
      });
    return sum.get();
  }

}
