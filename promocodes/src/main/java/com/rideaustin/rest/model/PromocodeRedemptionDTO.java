package com.rideaustin.rest.model;

import java.math.BigDecimal;
import java.util.Date;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class PromocodeRedemptionDTO {

  @ApiModelProperty(required = true)
  private final String codeLiteral;
  @ApiModelProperty(required = true)
  private final BigDecimal codeValue;
  @ApiModelProperty(required = true)
  private final Date createdDate;
  @ApiModelProperty(required = true)
  private final Date expiresOn;
  @ApiModelProperty(required = true)
  private final BigDecimal remainingValue;
  @ApiModelProperty(required = true)
  private final int timesUsed;
  @ApiModelProperty(required = true)
  private final int maximumUses;

  @QueryProjection
  public PromocodeRedemptionDTO(String codeLiteral, BigDecimal codeValue, Date createdDate, Date expiresOn,
    BigDecimal remainingValue, int timesUsed, int maximumUses) {
    this.codeLiteral = codeLiteral;
    this.codeValue = codeValue;
    this.createdDate = createdDate;
    this.expiresOn = expiresOn;
    this.remainingValue = remainingValue;
    this.timesUsed = timesUsed;
    this.maximumUses = maximumUses;
  }
}
