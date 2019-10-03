package com.rideaustin.model.redis;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.rideaustin.Constants;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.service.user.CarTypesUtils;
import com.rideaustin.utils.SurgeUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@RedisHash
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisSurgeArea {

  private static final String DEFAULT_CAR_CATEGORY = "REGULAR";

  @Id
  @ApiModelProperty(required = true, example = "1")
  private Long id;
  @ApiModelProperty(required = true)
  private String name;
  @ApiModelProperty(required = true)
  @JsonProperty("carCategoriesFactors")
  private Map<String, BigDecimal> surgeMapping = new TreeMap<>();
  @ApiModelProperty(required = true)
  private Map<String, BigDecimal> recommendedSurgeMapping = new TreeMap<>();
  @ApiModelProperty(required = true)
  @JsonProperty("carCategoriesRequestedRides")
  private Map<String, Integer> numberOfRequestedRides = new TreeMap<>();
  @ApiModelProperty(required = true)
  @JsonProperty("carCategoriesAcceptedRides")
  private Map<String, Integer> numberOfAcceptedRides = new TreeMap<>();
  @ApiModelProperty(required = true)
  @JsonProperty("carCategoriesCars")
  private Map<String, Integer> numberOfCars = new TreeMap<>();
  @ApiModelProperty(required = true)
  @JsonProperty("carCategoriesAvailableCars")
  private Map<String, Integer> numberOfAvailableCars = new TreeMap<>();
  @ApiModelProperty(required = true)
  @JsonProperty("carCategoriesNumberOfEyeballs")
  private Map<String, Integer> carCategoriesNumberOfEyeballs = new TreeMap<>();
  @ApiModelProperty(required = true, example = "1")
  private int carCategoriesBitmask;
  @ApiModelProperty(required = true)
  private boolean automated = false;
  @JsonUnwrapped
  @ApiModelProperty(required = true)
  private AreaGeometry areaGeometry;
  @Indexed
  @ApiModelProperty(required = true, example = "1")
  private Long cityId;

  public RedisSurgeArea(SurgeArea area) {
    this.id = area.getId();
    updateFieldsFrom(area);
  }

  public final void updateFieldsFrom(SurgeArea area) {
    Map<String, BigDecimal> newSurgeMapping = SurgeUtils.createSurgeMapping(area.getSurgeFactors(), area.getCarCategoriesBitmask());
    this.name = area.getName();
    this.cityId = area.getCityId();
    this.carCategoriesBitmask = area.getCarCategoriesBitmask();
    this.surgeMapping = new TreeMap<>(newSurgeMapping);
    this.recommendedSurgeMapping = new TreeMap<>(newSurgeMapping);
    this.areaGeometry = new AreaGeometry(area.getAreaGeometry());
    this.automated = area.isAutomated();
  }

  public BigDecimal getSurgeFactor(CarType carType) {
    return getSurgeFactor(carType.getCarCategory());
  }

  public BigDecimal getSurgeFactor(String carCategory) {
    return surgeMapping.getOrDefault(carCategory, Constants.NEUTRAL_SURGE_FACTOR);
  }

  public boolean isMandatory(CarType carType) {
    return supports(carType)
      && Constants.NEUTRAL_SURGE_FACTOR.compareTo(getSurgeFactor(carType.getCarCategory())) < 0;
  }

  public boolean supports(CarType carType) {
    return (carType.getBitmask() & carCategoriesBitmask) > 0;
  }

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public Set<String> getCarCategories() {
    return CarTypesUtils.fromBitMask(getCarCategoriesBitmask());
  }

}