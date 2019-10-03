package com.rideaustin.dispatch.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.TaskScheduler;

import com.rideaustin.dispatch.tasks.UpfrontChargeTask;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.RideTrackerService;
import com.rideaustin.service.RiderLocationService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class StartRideActionTest extends PersistingContextSupport {

  private static final long RIDE_ID = 1L;
  private static final long RIDER_ID = 1L;

  @Mock
  private RideTrackerService rideTrackerService;
  @Mock
  private RideFlowPushNotificationFacade pushNotificationFacade;
  @Mock
  private RiderLocationService riderLocationService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RidePaymentConfig ridePaymentConfig;
  @Mock
  private TaskScheduler scheduler;
  @Mock
  private BeanFactory beanFactory;

  @InjectMocks
  private StartRideAction testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new StartRideAction();

    MockitoAnnotations.initMocks(this);

    context.getExtendedState().getVariables().put("rideId", RIDE_ID);
    requestContext.setRiderId(RIDER_ID);
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
    StateMachineUtils.updateFlowContext(context, flowContext, persister, environment);
  }

  @Test
  public void executeSetsStartDate() {
    final Date startDate = new Date();
    context.addMessageHeader("startDate", startDate);

    testedInstance.execute(context);

    assertEquals(startDate, flowContext.getStartedOn());
  }

  @Test
  public void executeSetsRideUnstacked() {
    final Date startDate = new Date();
    context.addMessageHeader("startDate", startDate);

    testedInstance.execute(context);

    assertFalse(flowContext.isStacked());
  }

  @Test
  public void executeSavesFirstTracker() {
    final Date startDate = new Date();
    context.addMessageHeader("startDate", startDate);
    final double lat = 34.19916;
    final double lng = -97.984168;
    requestContext.setStartLocationLat(lat);
    requestContext.setStartLocationLong(lng);

    testedInstance.execute(context);

    verify(rideTrackerService).updateRideTracker(eq(RIDE_ID), argThat(new BaseMatcher<RideTracker>(){

      @Override
      public boolean matches(Object o) {
        final RideTracker rideTracker = (RideTracker) o;
        return rideTracker.getLatitude().equals(lat) && rideTracker.getLongitude().equals(lng);
      }

      @Override
      public void describeTo(Description description) {

      }
    }), eq(startDate));
  }

  @Test
  public void executeEvictsRiderLocationFromCache() {
    final Date startDate = new Date();
    context.addMessageHeader("startDate", startDate);

    testedInstance.execute(context);

    verify(riderLocationService).evictRiderLocation(eq(RIDER_ID));
  }

  @Test
  public void executeNotifiesRider() {
    final Date startDate = new Date();
    context.addMessageHeader("startDate", startDate);

    testedInstance.execute(context);

    verify(pushNotificationFacade).sendRideUpdateToRider(eq(RIDE_ID), eq(RideStatus.ACTIVE));
  }

  @Test
  public void executeSetsRideAsActive() {
    final Date startDate = new Date();
    context.addMessageHeader("startDate", startDate);

    testedInstance.execute(context);

    verify(rideDslRepository).setStatus(eq(RIDE_ID), eq(RideStatus.ACTIVE));
  }

  @Test
  public void executeSchedulesUpfrontPayment() {
    final Date startDate = new Date();
    context.addMessageHeader("startDate", startDate);
    when(ridePaymentConfig.isUpfrontPricingEnabled()).thenReturn(true);
    final UpfrontChargeTask task = mock(UpfrontChargeTask.class);
    when(beanFactory.getBean(UpfrontChargeTask.class)).thenReturn(task);
    when(task.withRideId(anyLong())).thenCallRealMethod();

    testedInstance.execute(context);

    verify(scheduler).schedule(any(UpfrontChargeTask.class), any(Date.class));
  }
}