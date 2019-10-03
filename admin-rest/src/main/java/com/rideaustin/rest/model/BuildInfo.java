package com.rideaustin.rest.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ApiModel
@RequiredArgsConstructor
public class BuildInfo {

  @ApiModelProperty(required = true)
  private final String projectVersion;
  @ApiModelProperty(required = true)
  private final String buildNumber;
  @ApiModelProperty(required = true)
  private final String gitCommit;
  @ApiModelProperty(required = true)
  private final String gitBranch;

}
