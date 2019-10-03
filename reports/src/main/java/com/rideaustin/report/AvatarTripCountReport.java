package com.rideaustin.report;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.reports.AvatarTripCountReportResultEntry;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.entry.AvatarTripCountReportEntry;
import com.rideaustin.report.params.CompletedOnBeforeReportParams;
import com.rideaustin.utils.DateUtils;

public abstract class AvatarTripCountReport<T extends AvatarTripCountReportEntry, P extends CompletedOnBeforeReportParams> extends BaseReport<T, P> {

  protected final RideReportDslRepository reportRepository;

  protected AvatarTripCountReport(RideReportDslRepository reportRepository) {
    this.reportRepository = reportRepository;
  }

  @Override
  protected void doExecute() {
    List<T> result = new ArrayList<>();
    List<AvatarTripCountReportResultEntry> rawData = getRawData();
    Map<Long, Map<LocalDate, List<AvatarTripCountReportResultEntry>>> groupedData = rawData.stream()
      .collect(groupingBy(AvatarTripCountReportResultEntry::getAvatarId, groupingBy(e -> DateUtils.getEndOfWeek(e.getCompletedOn()))));
    for (Map.Entry<Long, Map<LocalDate, List<AvatarTripCountReportResultEntry>>> idEntry : groupedData.entrySet()) {
      Long avatarId = idEntry.getKey();
      for (Map.Entry<LocalDate, List<AvatarTripCountReportResultEntry>> weekEntry : idEntry.getValue().entrySet()) {
        LocalDate endOfWeek = weekEntry.getKey();
        List<AvatarTripCountReportResultEntry> data = weekEntry.getValue();
        String firstName = data.get(0).getFirstName();
        String lastName = data.get(0).getLastName();
        String email = data.get(0).getEmail();
        Long tripCountPerWeek = (long) data.size();
        result.add(createEntry(avatarId, endOfWeek, firstName, lastName, email, tripCountPerWeek));
      }
    }
    this.resultsStream = result.stream()
      .sorted(comparing(AvatarTripCountReportEntry::getEndOfWeek).thenComparing(AvatarTripCountReportEntry::getAvatarId));
  }

  @Override
  public void setParameters(String parameters, Class<P> parameterClass) throws IOException {
    super.setParameters(parameters, parameterClass);
    this.parameters.setCompletedOnBefore(DateUtils.getEndOfTheDay(this.parameters.getCompletedOnBefore()));
  }

  protected Map<String, Object> createContext() {
    return ImmutableMap.of(
      "endDate", Constants.DATETIME_FORMATTER.format(parameters.getCompletedOnBefore())
    );
  }

  protected abstract T createEntry(Long avatarId, LocalDate endOfWeek, String firstName, String lastName, String email,
    Long tripCountPerWeek);
  protected abstract List<AvatarTripCountReportResultEntry> getRawData();
}
