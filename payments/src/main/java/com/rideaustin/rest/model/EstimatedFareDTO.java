package com.rideaustin.rest.model;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.rideaustin.service.model.DistanceTimeEstimation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class EstimatedFareDTO {
  @JsonUnwrapped
  private final CarCategoryEstimate defaultEstimate;
  @ApiModelProperty(notes = "Map of CarCategoryEstimate per car category", dataType = "Map")
  private final Map<String, CarCategoryEstimate> estimates;

  @JsonIgnore
  private final DistanceTimeEstimation distanceTimeEstimation;

  public EstimatedFareDTO(DistanceTimeEstimation distanceTimeEstimation, Money defaultTotalFare, CampaignBannerDto defaultCampaignInfo, Map<String, Pair<Money, CampaignBannerDto>> estimates) {
    this.distanceTimeEstimation = distanceTimeEstimation;
    this.defaultEstimate = new CarCategoryEstimate(defaultTotalFare, defaultCampaignInfo);
    this.estimates = estimates.entrySet()
      .stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> new CarCategoryEstimate(e.getValue().getKey(), e.getValue().getRight())));
  }

  @JsonProperty
  public Long getDuration() {
    return distanceTimeEstimation.getDistanceTime().getTime();
  }

  @Getter
  @ApiModel
  public static class CarCategoryEstimate {
    @ApiModelProperty(notes = "Total estimated fare")
    private final Money totalFare;
    @ApiModelProperty(notes = "Information on available ride campaign")
    private final CampaignBannerDto campaignInfo;

    CarCategoryEstimate(Money totalFare, CampaignBannerDto campaignInfo) {
      this.totalFare = totalFare;
      this.campaignInfo = campaignInfo;
    }
  }
}
