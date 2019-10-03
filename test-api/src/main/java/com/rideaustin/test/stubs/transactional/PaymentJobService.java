package com.rideaustin.test.stubs.transactional;

import com.rideaustin.service.SchedulerService;

public class PaymentJobService extends com.rideaustin.service.PaymentJobService {

  public PaymentJobService(SchedulerService schedulerService) {
    super(schedulerService);
  }

  @Override
  public void afterCommit(Action action) {
    try {
      action.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
