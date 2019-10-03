package com.rideaustin.dispatch.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class UpdateCommentActionTest extends PersistingContextSupport {

  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private EventsNotificationService eventsNotificationService;

  @InjectMocks
  private UpdateCommentAction testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new UpdateCommentAction();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeSetsRideComment() {
    final Ride ride = new Ride();
    final String comment = "Hello";
    context.getExtendedState().getVariables().put("rideId", 1L);
    context.addMessageHeader("comment", comment);
    when(rideDslRepository.findOne(anyLong())).thenReturn(ride);

    testedInstance.execute(context);

    assertEquals(comment, ride.getComment());
  }

  @Test
  public void executeNotifiesDriver() {
    final Ride ride = new Ride();
    final String comment = "Hello";
    context.getExtendedState().getVariables().put("rideId", 1L);
    context.addMessageHeader("comment", comment);
    final DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(1L);
    dispatchContext.setCandidate(candidate);
    when(rideDslRepository.findOne(anyLong())).thenReturn(ride);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);

    testedInstance.execute(context);

    verify(eventsNotificationService).sendRideUpdateToDriver(ride, dispatchContext.getCandidate(), EventType.RIDER_COMMENT_UPDATED);
  }
}