package com.rideaustin.report;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.Area;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.AirportRidesReportEntry;
import com.rideaustin.report.enums.AirportToReport;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.AirportRidesReportParams;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.model.BoundingBox;
import com.rideaustin.utils.DateUtils;
import com.rideaustin.utils.GeometryUtils;

import lombok.RequiredArgsConstructor;

@ReportComponent(
  id = 1,
  name = "Airport rides report",
  description = "Airport rides report",
  header = "Airport rides report for {startDate} - {endDate} and {airport}",
  parameters = {
    @ReportComponent.Param(
      label = "Airport",
      name = "airport",
      type = ReportParameterType.ENUM,
      enumClass = AirportToReport.class,
      required = true,
      order = 1
    ),
    @ReportComponent.Param(
      label = "Started on after",
      name = "startDate",
      type = ReportParameterType.DATETIME,
      required = true,
      order = 2
    ),
    @ReportComponent.Param(
      label = "Started on before",
      name = "endDate",
      type = ReportParameterType.DATETIME,
      required = true,
      order = 3
    ),
    @ReportComponent.Param(
      label = "Longitude from",
      name = "airportLongitudeFrom",
      type = ReportParameterType.DECIMAL,
      internal = true,
      order = 4
    ),
    @ReportComponent.Param(
      label = "Longitude to",
      name = "airportLongitudeTo",
      type = ReportParameterType.DECIMAL,
      internal = true,
      order = 5
    ),
    @ReportComponent.Param(
      label = "Latitude from",
      name = "airportLatitudeFrom",
      type = ReportParameterType.DECIMAL,
      internal = true,
      order = 6
    ),
    @ReportComponent.Param(
      label = "Latitude to",
      name = "airportLatitudeTo",
      type = ReportParameterType.DECIMAL,
      internal = true,
      order = 7
    )
  }
)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AirportRidesReport extends BaseReport<AirportRidesReportEntry, AirportRidesReportParams> {

  private final RideReportDslRepository rideReportDslRepository;
  private final AreaService areaService;

  @Override
  protected void doExecute() {
    List<Date> rideStarts = getRideStarts();
    this.resultsStream = rideStarts
      .stream()
      .map(DateUtils::dateToInstant)
      .map(i -> i.atZone(Constants.CST_ZONE))
      .map(ZonedDateTime::toLocalDate)
      .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
      .entrySet()
      .stream()
      .map(e -> new AirportRidesReportEntry(e.getKey(), e.getValue()))
      .sorted(Comparator.comparing(AirportRidesReportEntry::getCount));
  }

  private List<Date> getRideStarts() {
    Set<BoundingBox> areaBoundingBoxes = loadAirportBoundingBox();
    List<Date> result = new ArrayList<>();
    for (BoundingBox box : areaBoundingBoxes) {
      result.addAll(rideReportDslRepository.getRideStartByDateAndLocation(
        box.getTopLeftCorner().lat, box.getTopLeftCorner().lng,
        box.getBottomRightCorner().lat, box.getBottomRightCorner().lng,
        Date.from(parameters.getStartDate()), Date.from(parameters.getEndDate())));
    }
    return result;
  }

  private Set<BoundingBox> loadAirportBoundingBox() {
    String[] keys;
    if (parameters.getAirport() == AirportToReport.ALL) {
      keys = Arrays.stream(AirportToReport.values()).filter(v -> v != AirportToReport.ALL).map(Enum::name).toArray(String[]::new);
    } else {
      keys = new String[]{parameters.getAirport().name()};
    }
    return areaService.getByKeys(keys)
      .stream()
      .map(Area::getAreaGeometry)
      .map(AreaGeometry::getCsvGeometry)
      .map(GeometryUtils::buildCoordinates)
      .map(GeometryUtils::getBoundingBox)
      .collect(Collectors.toSet());
  }

  @Override
  protected ReportAdapter<AirportRidesReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(AirportRidesReportEntry.class,
      ImmutableMap.of("airport", parameters.getAirport(), "startDate", parameters.getStartDate(), "endDate", parameters.getEndDate()));
  }

}
