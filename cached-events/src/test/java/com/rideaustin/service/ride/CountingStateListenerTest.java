package com.rideaustin.service.ride;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import com.rideaustin.service.ride.SequentialRideEventsDispatcher.CountingStateListener;

public class CountingStateListenerTest {

  @Mock
  private DeferredResult<ResponseEntity> result;

  private CountingStateListener testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CountingStateListener(result);
  }

  @Test
  public void transitionEndedSetsResultWhenExpectedTransitionIsLessThanOccurred() {
    testedInstance.addExpectedTransitions(1);
    testedInstance.shouldSetResult(true);

    testedInstance.transitionEnded(null);

    verify(result).setResult(ResponseEntity.ok().build());
  }

}
