package com.rideaustin.assemblers;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.rideaustin.model.SupportTopic;
import com.rideaustin.rest.model.SupportTopicDto;

@Component
public class SupportTopicAssembler implements BilateralAssembler<SupportTopic, SupportTopicDto> {

  @Override
  public SupportTopicDto toDto(SupportTopic supportTopic) {
    return SupportTopicDto.builder()
      .avatarType(supportTopic.getAvatarType())
      .description(supportTopic.getDescription())
      .id(supportTopic.getId())
      .followUpTypes(supportTopic.getFollowUpTypes())
      .hasChildren(!supportTopic.getSubTopics().isEmpty())
      .hasForms(Objects.nonNull(supportTopic.getForm()))
      .build();
  }

  @Override
  public SupportTopic toDs(SupportTopicDto dto) {
    return SupportTopic.builder()
      .id(dto.getId())
      .avatarType(dto.getAvatarType())
      .description(dto.getDescription())
      .parent(this.toDs(dto.getParent()))
      .followUpTypes(dto.getFollowUpTypes())
      .build();
  }
}
