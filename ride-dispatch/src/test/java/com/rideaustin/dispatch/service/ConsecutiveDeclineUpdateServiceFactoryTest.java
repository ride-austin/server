package com.rideaustin.dispatch.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;

import com.rideaustin.dispatch.service.queue.QueueConsecutiveDeclineUpdateService;
import com.rideaustin.service.model.context.DispatchType;

public class ConsecutiveDeclineUpdateServiceFactoryTest {

  @Mock
  private BeanFactory beanFactory;
  @Mock
  private QueueConsecutiveDeclineUpdateService queuedImpl;
  @Mock
  private DefaultConsecutiveDeclineUpdateService defaultImpl;

  private ConsecutiveDeclineUpdateServiceFactory testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ConsecutiveDeclineUpdateServiceFactory(beanFactory);
  }

  @Test
  public void createQueuedService() {
    when(beanFactory.getBean(eq(QueueConsecutiveDeclineUpdateService.class))).thenReturn(queuedImpl);

    final ConsecutiveDeclineUpdateService result = testedInstance.createService(DispatchType.QUEUED);

    assertTrue(QueueConsecutiveDeclineUpdateService.class.isAssignableFrom(result.getClass()));
  }

  @Test
  public void createDefaultService() {
    when(beanFactory.getBean(eq("consecutiveDeclineUpdateService"), eq(DefaultConsecutiveDeclineUpdateService.class)))
      .thenReturn(defaultImpl);

    final ConsecutiveDeclineUpdateService result = testedInstance.createService(DispatchType.REGULAR);

    assertTrue(DefaultConsecutiveDeclineUpdateService.class.isAssignableFrom(result.getClass()));
  }
}