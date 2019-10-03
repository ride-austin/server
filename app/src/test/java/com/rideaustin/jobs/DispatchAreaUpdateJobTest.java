package com.rideaustin.jobs;

import static org.mockito.Mockito.any;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.service.areaqueue.AreaQueueUpdateService;
import com.rideaustin.service.areaqueue.AreaService;

@RunWith(MockitoJUnitRunner.class)
public class DispatchAreaUpdateJobTest {

  @Mock
  private AreaQueueUpdateService areaQueueService;

  @Mock
  private AreaService areaService;

  private DispatchAreaUpdateJob dispatchAreaUpdateJob;

  @Before
  public void setup() {

    dispatchAreaUpdateJob = new DispatchAreaUpdateJob();
    dispatchAreaUpdateJob.setAreaService(areaService);
    dispatchAreaUpdateJob.setAreaQueueUpdateService(areaQueueService);
  }

  @Test
  public void testExecuteInternal() {
    dispatchAreaUpdateJob.executeInternal();
  }

  @Test
  public void testExecuteInternalWithError() throws Exception {
    Mockito.doThrow(new NullPointerException()).when(areaQueueService).updateStatuses(any());
    dispatchAreaUpdateJob.executeInternal();
  }

}
