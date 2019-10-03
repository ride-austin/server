package com.rideaustin.report.params;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.Campaign;
import com.rideaustin.repo.dsl.CampaignDslRepository;
import com.rideaustin.report.model.AvailableValue;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.model.ReportParameterType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CampaignCompositeReportParamsProvider implements ReportParameterProvider {

  private final CampaignDslRepository campaignDslRepository;

  @Override
  public Set<? extends ReportParameter> createParams() {
    final Map<Long, String> campaigns = campaignDslRepository.findAll().stream().collect(Collectors.toMap(BaseEntity::getId, Campaign::getName));
    final ImmutableSet.Builder<ReportParameter> builder = ImmutableSet.<ReportParameter>builder()
      .add(new ReportParameter("Requested on after", "startDate", "",
        ReportParameterType.DATETIME, true, false, null, 2, null))
      .add(new ReportParameter("Requested on before", "endDate", "",
        ReportParameterType.DATETIME, true, false, null, 2, null))
      .add(new CampaignReportParameter(campaigns));
    return builder.build();
  }

  public static class CampaignReportParameter extends ReportParameter {

    private final Map<Long, String> campaigns;

    public CampaignReportParameter(Map<Long, String> campaigns) {
      super("Campaign", "campaign", "", ReportParameterType.ENUM, true, false, null, 3, null);
      this.campaigns = campaigns;
    }

    @Override
    public Set getAvailableValues() {
      return campaigns.entrySet()
        .stream()
        .map(AvailableValue::new)
        .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      CampaignReportParameter that = (CampaignReportParameter) o;
      return Objects.equals(campaigns, that.campaigns);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), campaigns);
    }
  }
}
