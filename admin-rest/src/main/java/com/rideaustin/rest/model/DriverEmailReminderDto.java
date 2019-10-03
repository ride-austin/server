package com.rideaustin.rest.model;

import java.util.Set;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@ApiModel
@AllArgsConstructor
public class DriverEmailReminderDto {
  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final String name;
  @ApiModelProperty(required = true)
  private final long cityId;
  @ApiModelProperty(required = true)
  private final Set<ExtraField> extraFields;

  @Getter
  @ApiModel
  @EqualsAndHashCode
  @AllArgsConstructor
  public static class ExtraField {
    @ApiModelProperty(required = true)
    private final String id;
    @ApiModelProperty(required = true)
    private final String name;
    @ApiModelProperty(required = true)
    private final ExtraFieldType type;
    @ApiModelProperty(required = true)
    private final int order;

    public enum ExtraFieldType {
      TEXT,
      TEXTAREA
    }
  }
}
