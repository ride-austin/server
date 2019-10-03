package com.rideaustin.service;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.model.SupportTopic;
import com.rideaustin.model.SupportTopicForm;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.repo.dsl.SupportTopicDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.ListSupportTopicParams;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SupportTopicService {

  @NonNull
  private SupportTopicDslRepository supportTopicDslRepository;

  public List<SupportTopic> listSupportTopics(ListSupportTopicParams searchCriteria) {
    return supportTopicDslRepository.findSupportTopics(searchCriteria);
  }

  public List<SupportTopic> listParentSupportTopicsByAvatarType(@Nonnull AvatarType avatarType) {
    return supportTopicDslRepository.findTopLevelForAvatarType(avatarType);
  }

  public SupportTopic getSupportTopic(Long supportTopicId) throws NotFoundException {
    SupportTopic topic = supportTopicDslRepository.findOne(supportTopicId);
    if (topic == null) {
      throw new NotFoundException("Support topic is not found");
    }
    return topic;
  }

  public SupportTopic createSupportTopic(SupportTopic updatedSupportTopic) throws RideAustinException {
    validateSupportTopicHierarchy(updatedSupportTopic);
    return supportTopicDslRepository.save(updatedSupportTopic);
  }

  public SupportTopic updateSupportTopic(Long supportTopicId, SupportTopic updatedSupportTopic) throws RideAustinException {
    validateSupportTopicHierarchy(updatedSupportTopic);
    SupportTopic supportTopic = supportTopicDslRepository.findOne(supportTopicId);
    if (supportTopic == null) {
      throw new NotFoundException("Support Topic not found");
    }
    return supportTopicDslRepository.save(updatedSupportTopic);
  }

  public void removeSupportTopic(Long supportTopicId) throws NotFoundException  {
    SupportTopic supportTopic = this.getSupportTopic(supportTopicId);
    supportTopic.setActive(false);
    supportTopicDslRepository.save(supportTopic);
  }

  public SupportTopicForm findForm(Long supportTopicId) {
    return supportTopicDslRepository.findOne(supportTopicId).getForm();
  }

  private void validateSupportTopicHierarchy(SupportTopic updatedSupportTopic) throws BadRequestException {
    if(updatedSupportTopic.getParent() != null && updatedSupportTopic.getParent().getParent() != null) {
      //avoid multiple hierachy levels
      throw new BadRequestException("Support topic hierarchy can't have more than two levels");
    }
  }
}
