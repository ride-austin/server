package com.rideaustin.rest.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.rideaustin.model.promocodes.PromocodeType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class is used as DTO for management console
 */
@Getter
@Setter
@Builder
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
public class PromocodeDto {

  @ApiModelProperty(required = true)
  private Long id;
  @ApiModelProperty(required = true)
  @Length(max = 50, message = "The title must be less than 50 characters")
  private String title;
  @ApiModelProperty(required = true)
  private PromocodeType promocodeType = PromocodeType.PUBLIC;
  private String codeLiteral;
  @ApiModelProperty(required = true)
  private BigDecimal codeValue;
  @ApiModelProperty(required = true)
  private Date startsOn;
  @ApiModelProperty(required = true)
  private Date endsOn;
  @ApiModelProperty(required = true)
  private boolean newRidersOnly;
  @ApiModelProperty(required = true)
  private Long maximumRedemption;
  @ApiModelProperty(required = true)
  private Long currentRedemption;
  @ApiModelProperty(required = true)
  private Integer maximumUsesPerAccount;
  @ApiModelProperty(required = true)
  private Long usageCount;
  @ApiModelProperty(required = true)
  private Long driverId;
  @ApiModelProperty(required = true)
  @NotEmpty(message = "Please select city")
  private Set<Long> cities;
  @ApiModelProperty(required = true)
  @NotEmpty(message = "Please select car category")
  private Set<String> carTypes;

  @ApiModelProperty(required = true)
  private Integer validForNumberOfRides;
  @ApiModelProperty(required = true)
  private Integer validForNumberOfDays;
  @ApiModelProperty(required = true)
  private Date useEndDate;
  @ApiModelProperty(required = true)
  private boolean nextTripOnly;
  @ApiModelProperty(required = true)
  private boolean applicableToFees;
  @ApiModelProperty(required = true)
  private BigDecimal maxPromotionValue;
  @ApiModelProperty(required = true)
  private BigDecimal cappedAmountPerUse;

}
