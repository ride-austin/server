package com.rideaustin.service;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.ride.jobs.ProcessRidePaymentJob;

public class PaymentJobServiceTest {

  @Mock
  private SchedulerService schedulerService;

  private PaymentJobService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new PaymentJobService(schedulerService);
  }

  @Test
  public void immediatelyTriggerPaymentJobCallsSchedulerService() throws ServerError {
    testedInstance.immediatelyTriggerPaymentJob(1L);

    verify(schedulerService).triggerJob(eq(ProcessRidePaymentJob.class),
      eq("1"), anyString(), argThat(new JobMapMatcher()));
  }

  @Test
  public void reschedulePaymentJobRemovesExistingJob() throws ServerError {
    when(schedulerService.checkIfExists(eq("1"), anyString())).thenReturn(true);
    TransactionSynchronizationManager.initSynchronization();

    testedInstance.reschedulePaymentJob(1L);
    TransactionSynchronizationManager.getSynchronizations().get(0).afterCommit();

    verify(schedulerService).removeJob(eq("1"), anyString());
    TransactionSynchronizationManager.clearSynchronization();
  }

  @Test
  public void reschedulePaymentJobSchedulesNewJob() throws ServerError {
    when(schedulerService.checkIfExists(eq("1"), anyString())).thenReturn(true);
    TransactionSynchronizationManager.initSynchronization();

    testedInstance.reschedulePaymentJob(1L);
    TransactionSynchronizationManager.getSynchronizations().get(0).afterCommit();

    verify(schedulerService).triggerJob(eq(ProcessRidePaymentJob.class),
      eq("1"), anyString(), argThat(new JobMapMatcher()));
    TransactionSynchronizationManager.clearSynchronization();
  }

  private static class JobMapMatcher extends BaseMatcher<Map<String, Object>> {
    @Override
    public boolean matches(Object o) {
      final Map<String, Object> map = (Map<String, Object>) o;
      return map.containsKey("rideId") && map.get("rideId").equals(1L);
    }

    @Override
    public void describeTo(Description description) {

    }
  }
}