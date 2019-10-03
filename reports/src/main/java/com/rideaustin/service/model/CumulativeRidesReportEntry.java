package com.rideaustin.service.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class CumulativeRidesReportEntry {

  @ApiModelProperty(required = true)
  private Page<DriverRidesReportEntry> driversRidesReport = new PageImpl<>(Collections.emptyList());
  @ApiModelProperty(required = true)
  private List<RideReportEntry> ridesReport = new ArrayList<>();

}
