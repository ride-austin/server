package com.rideaustin.repo.dsl;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.reports.TNCDriversHoursLoggedOnReportResult;
import com.rideaustin.model.ride.QActiveDriver;
import com.rideaustin.model.ride.QCar;
import com.rideaustin.model.user.QDriver;
import com.rideaustin.report.entry.DriversExportReportEntry;
import com.rideaustin.report.entry.FingerprintStatusReportEntry;
import com.rideaustin.rest.model.ListDriversParams;

@Repository
public class DriverReportDslRepository extends AbstractDslRepository {

  private static final QDriver qDriver = QDriver.driver;

  public List<FingerprintStatusReportEntry> findDriversWithFingerprintCleared(CityApprovalStatus fingerPrintCleared,
    Long... cities) {
    BooleanBuilder where = new BooleanBuilder();
    if (fingerPrintCleared != null) {
      where.and(qDriver.cityId.in(cities).and(qDriver.cityApprovalStatus.eq(fingerPrintCleared)));
    }
    return queryFactory
      .select(qDriver.id, qDriver.user.firstname, qDriver.user.middleName, qDriver.user.lastname, qDriver.user.email, qDriver.user.dateOfBirth, qDriver.cityApprovalStatus.eq(CityApprovalStatus.APPROVED))
      .from(qDriver)
      .where(where)
      .orderBy(qDriver.id.asc())
      .fetch()
      .stream()
      .map(FingerprintStatusReportEntry::new)
      .collect(Collectors.toList());
  }

  public Set<TNCDriversHoursLoggedOnReportResult> getDriversLoggedOn(Instant startDate, Instant endDate) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    return queryFactory
      .select(qActiveDriver.createdDate, qActiveDriver.updatedDate, qActiveDriver.driver.id)
      .from(qActiveDriver)
      .where(qActiveDriver.createdDate.between(Date.from(startDate), Date.from(endDate)))
      .fetch()
      .stream()
      .map(TNCDriversHoursLoggedOnReportResult::new)
      .collect(Collectors.toSet());
  }

  public Long getDriversLoggedOnCount(Instant startDate, Instant endDate) {
    QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
    return queryFactory.select(qActiveDriver.driver.id.countDistinct()).from(qActiveDriver)
      .where(qActiveDriver.createdDate.between(Date.from(startDate), Date.from(endDate)))
      .fetchOne();
  }

  public List<DriversExportReportEntry> exportDrivers(ListDriversParams listDriversParams) {
    BooleanBuilder builder = new BooleanBuilder();
    listDriversParams.fill(builder);
    QCar qCar = QCar.car;
    return queryFactory.select(
      qDriver.id, qDriver.agreementDate, qDriver.user.firstname, qDriver.user.lastname, qDriver.user.email,
      qDriver.user.phoneNumber, qDriver.active, qDriver.activationDate, qDriver.user.userEnabled,
      qCar.color, qCar.license, qCar.make, qCar.model, qCar.year, qCar.carCategoriesBitmask,
      qDriver.payoneerStatus
    ).from(qDriver)
      .innerJoin(qDriver.cars, qCar)
      .where(builder)
      .orderBy(qDriver.id.asc())
      .fetch()
      .stream()
      .map(DriversExportReportEntry::new)
      .collect(Collectors.toList());
  }
}
