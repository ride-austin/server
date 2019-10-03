package com.rideaustin.model;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel
@AllArgsConstructor
@NoArgsConstructor
public class ChangeDto {
  @ApiModelProperty(required = true)
  private Date revisionDate;
  @ApiModelProperty(required = true)
  private long entityId;
  @ApiModelProperty(required = true)
  private long revision;
  @ApiModelProperty(required = true)
  private String changedBy;
  @ApiModelProperty(required = true)
  private String entityName;
  @ApiModelProperty(required = true)
  private String changedFieldName;
  @ApiModelProperty(required = true)
  private String previousValue;
  @ApiModelProperty(required = true)
  private String newValue;
}
