package com.rideaustin.service.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.querydsl.core.Tuple;
import com.rideaustin.report.TupleConsumer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@ApiModel
@AllArgsConstructor
public class ZipCodeReportEntry implements TupleConsumer {
  @ApiModelProperty(required = true)
  private final String zipCode;
  @ApiModelProperty(required = true)
  private final Long rideCount;

  public ZipCodeReportEntry(Tuple tuple) {
    int index = -1;
    zipCode = getString(tuple, ++index);
    rideCount = getLong(tuple, ++index);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ZipCodeReportEntry that = (ZipCodeReportEntry) o;

    return new EqualsBuilder()
      .append(zipCode, that.zipCode)
      .append(rideCount, that.rideCount)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(zipCode)
      .append(rideCount)
      .toHashCode();
  }

}
