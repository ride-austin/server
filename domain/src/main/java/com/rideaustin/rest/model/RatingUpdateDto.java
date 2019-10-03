package com.rideaustin.rest.model;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.utils.DateTimeSerializer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class RatingUpdateDto {

  @ApiModelProperty(required = true)
  private final Long id;
  @ApiModelProperty(required = true)
  private final Long rideId;

  @ApiModelProperty(required = true)
  private final Long ratedId;
  @ApiModelProperty(required = true)
  private final String ratedFullName;

  @ApiModelProperty(required = true)
  private final Long ratedById;
  @ApiModelProperty(required = true)
  private final String ratedByFullName;

  @ApiModelProperty(required = true)
  private final double rating;
  @ApiModelProperty
  private final String comment;
  @ApiModelProperty(required = true)
  @JsonSerialize(using = DateTimeSerializer.class)
  private final Date createdDate;
  @ApiModelProperty(required = true)
  @JsonSerialize(using = DateTimeSerializer.class)
  private final Date updatedDate;

  @QueryProjection
  public RatingUpdateDto(Long id, Long rideId, Long ratedId, String ratedFullName, Long ratedById,
    String ratedByFullName, double rating, String comment, Date createdDate, Date updatedDate) {
    this.id = id;
    this.rideId = rideId;
    this.ratedId = ratedId;
    this.ratedFullName = ratedFullName;
    this.ratedById = ratedById;
    this.ratedByFullName = ratedByFullName;
    this.rating = rating;
    this.comment = comment;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
  }
}
