package com.rideaustin.report.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ApiModel
@RequiredArgsConstructor
public class ReportParameter {

  @ApiModelProperty(required = true)
  private final String parameterLabel;

  @ApiModelProperty(required = true)
  private final String parameterName;

  @ApiModelProperty(required = true)
  private final String parameterDescription;

  @ApiModelProperty(required = true)
  private final ReportParameterType parameterType;

  @ApiModelProperty(required = true)
  private final boolean required;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final boolean internal;

  @ApiModelProperty(required = true)
  private final String defaultValue;

  @ApiModelProperty(required = true)
  private final int order;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final String enumClass;

  @JsonProperty
  @ApiModelProperty
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Set getAvailableValues() throws ClassNotFoundException {
    if (enumClass == null) {
      return Collections.emptySet();
    }
    Class clazz = Class.forName(enumClass);
    return EnumSet.allOf(clazz);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ReportParameter)) {
      return false;
    }
    ReportParameter that = (ReportParameter) o;
    return required == that.required &&
      internal == that.internal &&
      order == that.order &&
      Objects.equals(parameterLabel, that.parameterLabel) &&
      Objects.equals(parameterName, that.parameterName) &&
      Objects.equals(parameterDescription, that.parameterDescription) &&
      parameterType == that.parameterType &&
      Objects.equals(defaultValue, that.defaultValue) &&
      Objects.equals(enumClass, that.enumClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parameterLabel, parameterName, parameterDescription, parameterType, required, internal, defaultValue, order, enumClass);
  }
}
