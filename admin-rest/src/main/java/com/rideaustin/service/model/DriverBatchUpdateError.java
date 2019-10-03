package com.rideaustin.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class DriverBatchUpdateError {

  @ApiModelProperty
  private int rowNumber;
  @ApiModelProperty
  private final String field;
  @ApiModelProperty
  private final String value;
  @ApiModelProperty
  private final String message;

  public DriverBatchUpdateError(String message) {
    this(0, null, null, message);
  }

  public DriverBatchUpdateError(int rowNumber, String field, String value, String message) {
    this.rowNumber = rowNumber + 1;
    this.field = field;
    this.value = value;
    this.message = message;
  }

  public int getRowNumber() {
    return rowNumber;
  }

  public void setRowNumber(int rowNumber) {
    this.rowNumber = rowNumber;
  }

  public String getField() {
    return field;
  }

  public String getValue() {
    return value;
  }

  public String getMessage() {
    return message;
  }
}
