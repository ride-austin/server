package com.rideaustin.model.rest;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class CampaignProviderDto {

  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final String menuTitle;
  @ApiModelProperty(required = true)
  private final String menuIcon;

  @QueryProjection
  public CampaignProviderDto(long id, String menuTitle, String menuIcon) {
    this.id = id;
    this.menuTitle = menuTitle;
    this.menuIcon = menuIcon;
  }
}
