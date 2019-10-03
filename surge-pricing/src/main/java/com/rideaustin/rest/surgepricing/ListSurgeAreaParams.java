package com.rideaustin.rest.surgepricing;

import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.rideaustin.Constants;
import com.rideaustin.model.redis.RedisSurgeArea;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ListSurgeAreaParams {

  @ApiModelProperty("Surge area name")
  private String name;

  @ApiModelProperty("Set of surge area names")
  private Set<String> names;

  @ApiModelProperty(value = "Location latitude to search surge area at", example = "30.286804")
  private Double latitude;

  @ApiModelProperty(value = "Location longitude to search surge area at", example = "-97.707425")
  private Double longitude;

  @ApiModelProperty(value = "City ID", example = "1")
  private Long cityId = Constants.DEFAULT_CITY_ID;

  public Predicate<RedisSurgeArea> filter() {

    Predicate<RedisSurgeArea> result = a -> true;

    if (!StringUtils.isEmpty(getName())) {
      result = result.and(sa -> sa.getName().contains(getName()));
    }
    if (!CollectionUtils.isEmpty(getNames())) {
      result = result.and(sa -> getNames().contains(sa.getName()));
    }
    return result;
  }

}
