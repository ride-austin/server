package com.rideaustin.test.asserts;

import java.util.LinkedList;
import java.util.Objects;

import org.assertj.core.api.AbstractAssert;

public class DispatchHistoryAssert extends AbstractAssert<DispatchHistoryAssert, LinkedList<Long>> {

  private int currentPosition = 0;

  private DispatchHistoryAssert(LinkedList<Long> history) {
    super(history, DispatchHistoryAssert.class);
  }

  public static DispatchHistoryAssert assertThat(LinkedList<Long> history) {
    return new DispatchHistoryAssert(history);
  }

  public DispatchHistoryAssert isEmpty() {
    return hasLength(0);
  }

  public DispatchHistoryAssert hasLength(int length) {
    isNotNull();
    if (!Objects.equals(actual.size(), length)) {
      failWithMessage("Expected length <%d> but was <%d>", length, actual.size());
    }
    return this;
  }

  public DispatchHistoryAssert isDispatchedFirstTo(long driverId) {
    currentPosition = 0;
    return isDispatchedTo(driverId, 0);
  }

  public DispatchHistoryAssert thenIsDispatchedTo(long driverId) {
    currentPosition++;
    return isDispatchedTo(driverId, currentPosition);
  }

  private DispatchHistoryAssert isDispatchedTo(long driverId, int position) {
    isNotNull();
    if (!Objects.equals(actual.get(position), driverId)) {
      failWithMessage("Expected length <%d> but was <%d>", driverId, actual.get(currentPosition));
    }
    return this;
  }
}
