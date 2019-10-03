package com.rideaustin.queue;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.EventType;

@Category(AirportQueue.class)
public class C1183004StayIT extends BaseC1183004Test {

  @Test
  public void testStay() throws Exception {
    doTest(inactiveTimeout * 60 - 1, EventType.QUEUED_AREA_ENTERING);
  }

}
