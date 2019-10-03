package com.rideaustin.report.model;

import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rideaustin.report.Report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ApiModel
@RequiredArgsConstructor
public class ReportMetadata {

  @ApiModelProperty(required = true)
  private final long id;

  @ApiModelProperty(required = true)
  private final String reportName;

  @ApiModelProperty(required = true)
  private final String reportDescription;

  @ApiModelProperty(required = true)
  private final ReportFormat reportFormat;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final String reportHeader;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Set<ReportParameter> parameters;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Boolean archive;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Boolean upload;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Class<? extends Report> reportClass;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ReportMetadata)) {
      return false;
    }
    ReportMetadata that = (ReportMetadata) o;
    return id == that.id &&
      Objects.equals(reportName, that.reportName) &&
      Objects.equals(reportDescription, that.reportDescription) &&
      reportFormat == that.reportFormat &&
      Objects.equals(reportHeader, that.reportHeader) &&
      Objects.equals(parameters, that.parameters) &&
      Objects.equals(archive, that.archive) &&
      Objects.equals(upload, that.upload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, reportName, reportDescription, reportFormat, reportHeader, parameters, archive, upload);
  }

  public Class<? extends Report> getReportClass() {
    return reportClass;
  }
}
