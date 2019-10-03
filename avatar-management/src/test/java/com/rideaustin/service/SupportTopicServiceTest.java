package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.rideaustin.model.SupportTopic;
import com.rideaustin.model.SupportTopicForm;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.repo.dsl.SupportTopicDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.ListSupportTopicParams;

public class SupportTopicServiceTest {

  @Mock
  private SupportTopicDslRepository supportTopicDslRepository;

  private SupportTopicService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new SupportTopicService(supportTopicDslRepository);
  }

  @Test
  public void listSupportTopicsDelegatesCallToRepository() {
    final ListSupportTopicParams searchCriteria = new ListSupportTopicParams();

    testedInstance.listSupportTopics(searchCriteria);

    verify(supportTopicDslRepository).findSupportTopics(searchCriteria);
  }

  @Test
  public void listParentSupportTopicsByAvatarTypeDelegatesCallToRepository() {
    final AvatarType avatarType = AvatarType.RIDER;

    testedInstance.listParentSupportTopicsByAvatarType(avatarType);

    verify(supportTopicDslRepository).findTopLevelForAvatarType(avatarType);
  }

  @Test(expected = NotFoundException.class)
  public void getSupportTopicThrowsNotFoundOnAbsentTopic() throws NotFoundException {
    testedInstance.getSupportTopic(1L);
  }

  @Test
  public void getSupportTopicDelegatesCallToRepository() throws NotFoundException {
    final SupportTopic supportTopic = new SupportTopic();
    when(supportTopicDslRepository.findOne(anyLong())).thenReturn(supportTopic);

    final SupportTopic result = testedInstance.getSupportTopic(1L);

    assertEquals(supportTopic, result);
  }

  @Test(expected = BadRequestException.class)
  public void createSupportTopicThrowsExceptionOnInvalidHierarchyDepth() throws RideAustinException {
    SupportTopic level1Topic = setupInvalidHierarchySupportTopic();

    testedInstance.createSupportTopic(level1Topic);
  }

  @Test
  public void createSupportTopicDelegatesCallToRepository() throws RideAustinException {
    SupportTopic supportTopic = new SupportTopic();

    testedInstance.createSupportTopic(supportTopic);

    verify(supportTopicDslRepository).save(supportTopic);
  }

  @Test(expected = BadRequestException.class)
  public void updateSupportTopicThrowsExceptionOnInvalidHierarchyDepth() throws RideAustinException {
    SupportTopic level1Topic = setupInvalidHierarchySupportTopic();

    testedInstance.updateSupportTopic(1L, level1Topic);
  }

  @Test(expected = NotFoundException.class)
  public void updateSupportTopicThrowsExceptionOnAbsentTopic() throws RideAustinException {
    when(supportTopicDslRepository.findOne(anyLong())).thenReturn(null);

    testedInstance.updateSupportTopic(1L, new SupportTopic());
  }

  @Test
  public void updateSupportTopicDelegatesCallToRepository() throws RideAustinException {
    final SupportTopic existing = new SupportTopic();
    when(supportTopicDslRepository.findOne(anyLong())).thenReturn(existing);
    when(supportTopicDslRepository.save(any(SupportTopic.class))).thenAnswer((Answer<SupportTopic>) invocation -> (SupportTopic) invocation.getArguments()[0]);
    final SupportTopic updatedSupportTopic = new SupportTopic();

    final SupportTopic result = testedInstance.updateSupportTopic(1L, updatedSupportTopic);

    assertEquals(updatedSupportTopic, result);
    verify(supportTopicDslRepository).save(updatedSupportTopic);
  }

  @Test(expected = NotFoundException.class)
  public void removeSupportTopicThrowsExceptionOnAbsentTopic() throws NotFoundException {
    testedInstance.removeSupportTopic(1L);
  }

  @Test
  public void removeSupportTopicDeactivatesTopic() throws NotFoundException {
    final SupportTopic supportTopic = new SupportTopic();
    when(supportTopicDslRepository.findOne(anyLong())).thenReturn(supportTopic);

    testedInstance.removeSupportTopic(1L);

    assertFalse(supportTopic.isActive());
    verify(supportTopicDslRepository).save(supportTopic);
  }

  @Test
  public void findFormDelegatesCallToRepository() {
    final SupportTopic supportTopic = new SupportTopic();
    final SupportTopicForm form = new SupportTopicForm();
    supportTopic.setForm(form);
    when(supportTopicDslRepository.findOne(anyLong())).thenReturn(supportTopic);

    final SupportTopicForm result = testedInstance.findForm(1L);

    assertEquals(form, result);
  }


  private SupportTopic setupInvalidHierarchySupportTopic() {
    final SupportTopic level2Topic = new SupportTopic();
    final SupportTopic level3Topic = new SupportTopic();
    SupportTopic level1Topic = new SupportTopic();
    level1Topic.setParent(level2Topic);
    level2Topic.setParent(level3Topic);
    return level1Topic;
  }
}