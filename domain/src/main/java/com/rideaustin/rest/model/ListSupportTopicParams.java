package com.rideaustin.rest.model;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.QSupportTopic;
import com.rideaustin.model.SupportTopic;
import com.rideaustin.model.enums.AvatarType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
public class ListSupportTopicParams {

  private static final QSupportTopic qSupportTopic = QSupportTopic.supportTopic;

  @Getter
  @Setter
  @ApiModelProperty("Topic description")
  private String description;

  @Getter
  @Setter
  @ApiModelProperty(value = "Avatar type", allowableValues = "RIDER,DRIVER")
  private AvatarType avatarType;

  @Getter
  @Setter
  @ApiModelProperty(value = "Parent topic ID", example = "1")
  private Long parentTopicId;

  @Getter
  @Setter
  @ApiModelProperty("List active support topics")
  private Boolean active;

  public Predicate<SupportTopic> filter() {
    Predicate<SupportTopic> result = SupportTopic::isActive;

    if (!StringUtils.isEmpty(getDescription())) {
      result = result.and(st -> st.getDescription().contains(getDescription()));
    }
    if (getAvatarType() != null) {
      result = result.and(st -> getAvatarType().equals(st.getAvatarType()));
    }
    if (getParentTopicId() != null) {
      result = result.and(st -> getParentTopicId().equals(st.getParent().getId()));
    }
    return result;
  }

  public void fill(BooleanBuilder builder) {
    if (StringUtils.isNotEmpty(getDescription())) {
      builder.and(qSupportTopic.description.containsIgnoreCase(getDescription()).or(qSupportTopic.description.containsIgnoreCase(getDescription())));
    }
    if(getAvatarType() != null) {
      builder.and(qSupportTopic.avatarType.eq(getAvatarType()));
    }
    if(getParentTopicId() != null) {
      builder.and(qSupportTopic.parent.id.eq(getParentTopicId()));
    }
    if (getActive() != null) {
      builder.and(qSupportTopic.active.eq(getActive()));
    } else {
      builder.and(qSupportTopic.active.isTrue());
    }
  }
}
