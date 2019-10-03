package com.rideaustin.rest.model;

import java.math.BigDecimal;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.Constants;
import com.rideaustin.utils.SurgeFactors;
import com.rideaustin.utils.SurgeFactorsMax;
import com.rideaustin.utils.SurgeFactorsMin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SurgeAreaDto {

  @ApiModelProperty(required = true)
  private long id;

  @SurgeFactors
  @SurgeFactorsMin(Constants.NEUTRAL_SURGE_FACTOR_STR)
  @SurgeFactorsMax(Constants.MAXIMUM_SURGE_FACTOR_STR)
  @NotEmpty(message = "Surge factor must be set for at least one car category")
  @ApiModelProperty(value = "A String->BigDecimal map containing mapping between car category and surge factor", required = true)
  private Map<String, BigDecimal> surgeFactors;

  @ApiModelProperty(required = true)
  @NotNull(message = "Name may not be null")
  private String name;

  @ApiModelProperty(required = true)
  @NotNull(message = "topLeftCornerLat may not be null")
  private Double topLeftCornerLat;

  @ApiModelProperty(required = true)
  @NotNull(message = "topLeftCornerLng may not be null")
  private Double topLeftCornerLng;

  @ApiModelProperty(required = true)
  @NotNull(message = "bottomRightCornerLat may not be null")
  private Double bottomRightCornerLat;

  @ApiModelProperty(required = true)
  @NotNull(message = "bottomRightCornerLng may not be null")
  private Double bottomRightCornerLng;

  @ApiModelProperty(required = true)
  @NotNull(message = "centerPointLat may not be null")
  private Double centerPointLat;

  @ApiModelProperty(required = true)
  @NotNull(message = "centerPointLng may not be null")
  private Double centerPointLng;

  @ApiModelProperty(required = true)
  @NotNull(message = "csvGeometry may not be null")
  private String csvGeometry;

  @ApiModelProperty(required = true)
  @NotNull(message = "labelLat may not be null")
  private Double labelLat;

  @ApiModelProperty(required = true)
  @NotNull(message = "labelLng may not be null")
  private Double labelLng;

  @NotNull(message = "cityId may not be null")
  @ApiModelProperty(example = "1", required = true)
  private Long cityId;

  @ApiModelProperty
  private boolean automated = false;
}
