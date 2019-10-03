package com.rideaustin.jobs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rideaustin.Constants;
import com.rideaustin.model.Campaign;
import com.rideaustin.repo.dsl.CampaignDslRepository;
import com.rideaustin.report.CampaignCompositeReport;
import com.rideaustin.rest.model.PeriodicReportType;
import com.rideaustin.utils.DateUtils;
import com.rideaustin.utils.ISO8601Serializer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Component
public class CampaignTripReportJob extends GenericReportJob {

  @Setter(onMethod = @__(@Inject))
  private Environment environment;
  @Setter(onMethod = @__(@Inject))
  private CampaignDslRepository repository;

  @Setter
  private PeriodicReportType type = PeriodicReportType.WEEKLY;

  public CampaignTripReportJob() {
    reportClass = CampaignCompositeReport.class;
  }

  @Override
  protected void executeInternal() throws JobExecutionException {
    try {
      String defaultRecipients = environment.getProperty("jobs.campaign_trips.recipients");
      for (Campaign campaignToReport : repository.findAll()) {
        String reportRecipients = campaignToReport.getReportRecipients();
        if (reportRecipients == null || !reportRecipients.contains(",")) {
          reportRecipients = defaultRecipients;
        }
        recipients = Arrays.asList(reportRecipients.split(","));
        if (StringUtils.isEmpty(paramsJson)) {
          final LocalDateTime startDate = LocalDate.now().atStartOfDay().with(type.getStartAdjuster());
          final Date start = DateUtils.localDateTimeToDate(startDate, Constants.CST_ZONE);
          final Date end = DateUtils.localDateTimeToDate(startDate.plus(type.getPeriod()), Constants.CST_ZONE);
          paramsJson = objectMapper.writeValueAsString(new Params(campaignToReport.getId(), start, end));
        }
        super.executeInternal();
        paramsJson = null;
      }
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }

  @Getter
  @AllArgsConstructor
  public static class Params {
    private long campaign;
    @JsonSerialize(using = ISO8601Serializer.class)
    private Date startDate;
    @JsonSerialize(using = ISO8601Serializer.class)
    private Date endDate;
  }
}
