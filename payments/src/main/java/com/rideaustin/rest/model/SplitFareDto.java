package com.rideaustin.rest.model;

import static com.rideaustin.Constants.CST_ZONE;

import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.SplitFareStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel
@AllArgsConstructor
@JsonDeserialize(builder = SplitFareDto.SplitFareDtoBuilder.class)
public class SplitFareDto {

  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final long rideId;
  @ApiModelProperty(required = true)
  private final long riderId;
  @ApiModelProperty(required = true)
  private final String riderFullName;
  @ApiModelProperty(required = true)
  private final String riderPhoto;
  @ApiModelProperty(required = true)
  private final SplitFareStatus status;
  @ApiModelProperty(required = true)
  private final String createdDate;
  @ApiModelProperty(required = true)
  private final String updatedDate;
  @ApiModelProperty(required = true)
  private final String sourceRiderFullName;
  @ApiModelProperty(required = true)
  private final String sourceRiderPhotoURL;

  @QueryProjection
  public SplitFareDto(Long id, Long rideId, Long riderId, String riderFullName, String riderPhoto, SplitFareStatus status,
    Date createdDate, Date updatedDate, String sourceRiderFullName, String sourceRiderPhotoURL) {
    this.id = id;
    this.rideId = rideId;
    this.riderId = riderId;
    this.riderFullName = riderFullName;
    this.riderPhoto = riderPhoto;
    this.status = status;
    this.createdDate = formatDate(createdDate);
    this.updatedDate = formatDate(updatedDate);
    this.sourceRiderFullName = sourceRiderFullName;
    this.sourceRiderPhotoURL = sourceRiderPhotoURL;
  }

  private String formatDate(Date date) {
    return DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(CST_ZONE).format(date.toInstant());
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class SplitFareDtoBuilder {}

}
