package com.rideaustin.driverstatistic.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DriverStatisticRepository extends JpaRepository<DriverStatistic, Long> {

  DriverStatistic findByDriverId(DriverId driverId);

}
