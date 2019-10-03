package com.rideaustin.rest.model;

import java.util.Set;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.FollowUpType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
public class SupportTopicDto {

  @ApiModelProperty(required = true)
  private long id;
  @ApiModelProperty(required = true)
  private String description;
  @ApiModelProperty(required = true)
  private AvatarType avatarType;
  @ApiModelProperty
  private SupportTopicDto parent;
  @ApiModelProperty(required = true)
  private Set<FollowUpType> followUpTypes;
  @ApiModelProperty(required = true)
  private boolean hasChildren;
  @ApiModelProperty(required = true)
  private boolean hasForms;

}
