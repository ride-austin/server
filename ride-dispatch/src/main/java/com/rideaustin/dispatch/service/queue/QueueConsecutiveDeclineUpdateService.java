package com.rideaustin.dispatch.service.queue;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.rideaustin.dispatch.service.DefaultConsecutiveDeclineUpdateService;
import com.rideaustin.dispatch.service.DispatchDeclineRequestChecker;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.areaqueue.AreaQueuePenaltyService;
import com.rideaustin.service.config.AreaQueueConfig;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.DispatchRequest;

public class QueueConsecutiveDeclineUpdateService extends DefaultConsecutiveDeclineUpdateService {

  private final AreaQueueConfig areaQueueConfig;
  private final AreaQueuePenaltyService penaltyService;

  @Inject
  public QueueConsecutiveDeclineUpdateService(ActiveDriverLocationService activeDriverLocationService,
    EventsNotificationService eventsNotificationService, ActiveDriversService activeDriversService,
    RideDispatchServiceConfig config, List<DispatchDeclineRequestChecker> checkers, AreaQueueConfig areaQueueConfig,
    AreaQueuePenaltyService penaltyService) {
    super(activeDriverLocationService, eventsNotificationService, activeDriversService, config, checkers);
    this.areaQueueConfig = areaQueueConfig;
    this.penaltyService = penaltyService;
  }

  @Override
  protected boolean processConsecutiveRideDecline(DispatchRequest request, Ride ride, Collection<DispatchDeclineRequestChecker> checkers) {
    boolean shouldDeactivate = super.processConsecutiveRideDecline(request, ride, checkers);
    if (shouldDeactivate) {
      penaltyService.penalize(request.getDriverId());
    }
    return shouldDeactivate;
  }

  @Override
  protected boolean shouldDeactivate(Integer declineCount) {
    return areaQueueConfig.getMaxDeclines() == declineCount;
  }
}
