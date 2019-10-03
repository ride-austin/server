package com.rideaustin.model.lostandfound;

import java.util.Date;

import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.LostAndFoundRequestType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class LostAndFoundRequestDto {

  @ApiModelProperty(required = true)
  private final LostAndFoundRequestType type;
  @ApiModelProperty(required = true)
  private final String content;
  @ApiModelProperty(required = true)
  private final Date createdOn;

  @QueryProjection
  public LostAndFoundRequestDto(LostAndFoundRequestType type, String content, Date createdOn) {
    this.type = type;
    this.content = content;
    this.createdOn = createdOn;
  }
}
