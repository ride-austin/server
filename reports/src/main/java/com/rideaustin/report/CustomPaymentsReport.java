package com.rideaustin.report;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.CustomPaymentReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.CustomPaymentsReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.CustomPaymentsReportParams;

import lombok.RequiredArgsConstructor;

@ReportComponent(
  id = 10,
  name = "Custom payment Report",
  header = "Custom payment Report",
  parameters = {
    @ReportComponent.Param(
      label = "Created before",
      name = "createdBefore",
      type = ReportParameterType.DATETIME,
      required = true,
      order = 1
    ),
    @ReportComponent.Param(
      label = "Created after",
      name = "createdAfter",
      type = ReportParameterType.DATETIME,
      required = true,
      order = 2
    ),
    @ReportComponent.Param(
      label = "Payment date",
      name = "paymentDate",
      type = ReportParameterType.DATETIME,
      order = 3
    ),
    @ReportComponent.Param(
      label = "City ID",
      name = "cityId",
      type = ReportParameterType.INTEGER,
      order = 4
    )
  }
)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CustomPaymentsReport extends BaseReport<CustomPaymentsReportEntry, CustomPaymentsReportParams> {

  private final CustomPaymentReportDslRepository customPaymentDslRepo;

  @Override
  protected ReportAdapter<CustomPaymentsReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(CustomPaymentsReportEntry.class, Collections.emptyMap());
  }

  @Override
  protected void doExecute() {
    this.resultsStream =
      customPaymentDslRepo.findBetweenDates(
        Date.from(parameters.getCreatedAfter()), Date.from(parameters.getCreatedBefore()), parameters.getCityId())
      .stream()
      .map(p -> new CustomPaymentsReportEntry(Optional.ofNullable(p.getDriver()).map(Driver::getPayoneerId).orElse(null),
        p.getValue(), p.getId(),
        Optional.ofNullable(p.getValue()).map(Money::getCurrencyUnit).map(CurrencyUnit::toString).orElse(null), p.getDescription(),
        Optional.ofNullable(parameters.getPaymentDate())
          .orElse(Optional.ofNullable(p.getPaymentDate()).map(Date::toInstant).orElse(null))));
  }
}
