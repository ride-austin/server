package com.rideaustin.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.Mock;

import com.rideaustin.Constants;
import com.rideaustin.model.Area;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.AirportRidesReportEntry;
import com.rideaustin.report.enums.AirportToReport;
import com.rideaustin.report.params.AirportRidesReportParams;
import com.rideaustin.service.areaqueue.AreaService;

public class AirportRidesReportTest extends AbstractReportTest<AirportRidesReport> {

  private static final Instant START_DATE = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay().toInstant(ZoneOffset.UTC);
  private static final Instant END_DATE = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
  @Mock
  private RideReportDslRepository rideReportDslRepository;

  @Mock
  private AreaService areaService;

  @Override
  protected AirportRidesReport doCreateTestedInstance() {
    return new AirportRidesReport(rideReportDslRepository, areaService);
  }

  @Test
  public void doExecute() throws Exception {
    testedInstance.parameters = createParameters();
    Area area = new Area();
    AreaGeometry areaGeometry = new AreaGeometry();
    areaGeometry.setCsvGeometry("-97.6806460,30.2112030 -97.6806460,30.1800798 -97.6560610,30.1800540 -97.6560610,30.2112030");
    area.setAreaGeometry(areaGeometry);
    area.setName("AUSTIN");
    when(areaService.getByKeys(any())).thenReturn(Collections.singletonList(area));
    when(rideReportDslRepository.getRideStartByDateAndLocation(anyDouble(), anyDouble(), anyDouble(), anyDouble(),
      any(Date.class), any(Date.class))).thenReturn(Arrays.asList(
      Date.from(LocalDate.of(2016, 8, 15).atStartOfDay().atZone(Constants.CST_ZONE).toInstant()),
      Date.from(LocalDate.of(2016, 8, 14).atStartOfDay().atZone(Constants.CST_ZONE).toInstant()),
      Date.from(LocalDate.of(2016, 8, 14).atStartOfDay().atZone(Constants.CST_ZONE).toInstant())
    ));

    testedInstance.doExecute();

    List<AirportRidesReportEntry> result = testedInstance.getResultsStream().collect(Collectors.toList());
    assertEquals(2, result.size());
    assertEquals(LocalDate.of(2016, 8, 15), result.get(0).getDate());
    assertEquals(LocalDate.of(2016, 8, 14), result.get(1).getDate());
    assertEquals(Long.valueOf(1L), result.get(0).getCount());
    assertEquals(Long.valueOf(2L), result.get(1).getCount());
  }

  @Test
  public void testGetAdapter() {
    testedInstance.parameters = createParameters();

    ReportAdapter<AirportRidesReportEntry> adapter = testedInstance.getAdapter();

    Map<String, Object> reportContext = adapter.getReportContext();

    assertEquals(START_DATE, reportContext.get("startDate"));
    assertEquals(END_DATE, reportContext.get("endDate"));
  }

  @Test
  public void testSetParameters() throws IOException {
    String jsonParams = "{\"airport\":\"Austin Bergstrom\",\"startDate\":\"2016-08-01T17:26:10.905Z\",\"endDate\":\"2016-08-16T17:26:10.905Z\"}";
    Class paramsClass = AirportRidesReportParams.class;

    testedInstance.setParameters(jsonParams, paramsClass);

    assertNotNull(testedInstance.parameters.getStartDate());
    assertNotNull(testedInstance.parameters.getEndDate());
  }

  private AirportRidesReportParams createParameters() {
    AirportRidesReportParams params = new AirportRidesReportParams();
    params.setAirportLatitudeFrom(0.0);
    params.setAirportLatitudeTo(0.0);
    params.setAirportLongitudeFrom(0.0);
    params.setAirportLongitudeTo(0.0);
    params.setStartDate(START_DATE);
    params.setEndDate(END_DATE);
    params.setAirport(AirportToReport.AUSTIN_BERGSTROM);
    return params;
  }

}