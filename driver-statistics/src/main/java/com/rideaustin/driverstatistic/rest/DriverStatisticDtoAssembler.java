package com.rideaustin.driverstatistic.rest;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.rideaustin.assemblers.SingleSideAssembler;
import com.rideaustin.driverstatistic.model.DriverStatistic;

@Component
public class DriverStatisticDtoAssembler implements SingleSideAssembler<DriverStatistic, List<DriverStatisticDto>> {

  @Override
  public List<DriverStatisticDto> toDto(DriverStatistic entity) {

    return ImmutableList.of(
      new DriverStatisticDto("Acceptance", "Rides accepted over last 100 dispatches", entity.getLastAcceptedCount(), entity.getLastAcceptedOver()),
      new DriverStatisticDto("Cancellation", "Rides cancelled over last 100 accepted dispatches", entity.getLastCancelledCount(), entity.getLastCancelledOver())
    );
  }
}
