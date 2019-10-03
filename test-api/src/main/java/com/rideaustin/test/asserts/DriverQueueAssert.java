package com.rideaustin.test.asserts;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;

import com.rideaustin.test.response.AreaQueuePositions;

public class DriverQueueAssert extends AbstractAssert<DriverQueueAssert, AreaQueuePositions> {

  private DriverQueueAssert(AreaQueuePositions areaQueuePositions) {
    super(areaQueuePositions, DriverQueueAssert.class);
  }

  public static DriverQueueAssert assertThat(AreaQueuePositions response) {
    return new DriverQueueAssert(response);
  }

  public DriverQueueAssert hasName(String name) {
    isNotNull();
    if (!Objects.equals(actual.getAreaQueueName(), name)) {
      failWithMessage("Expected queue name <%s> but was <%s>", name, actual.getAreaQueueName());
    }
    return this;
  }

  public DriverQueueAssert hasPosition(String category, int position) {
    isNotNull();
    if (!actual.getPositions().containsKey(category)) {
      failWithMessage("Queue doesn't have entries with %s category", category);
    }
    if (actual.getPositions().get(category) != position) {
      failWithMessage("Expected queue position <%s> but was <%s>", position, actual.getPositions().get(category));
    }
    return this;
  }

  public DriverQueueAssert hasLength(String category, int expected) {
    isNotNull();
    if (!actual.getLengths().containsKey(category)) {
      failWithMessage("Queue doesn't have entries with %s category", category);
    }
    if (actual.getLengths().get(category) != expected) {
      failWithMessage("Expected queue length <%s> but was <%s>", expected, actual.getLengths().get(category));
    }
    return this;
  }

  public DriverQueueAssert hasCategory(String category) {
    isNotNull();
    if (!actual.getPositions().containsKey(category)) {
      failWithMessage("Expected to be present in %s queue", category);
    }
    return this;
  }

  public DriverQueueAssert hasNotCategory(String category) {
    isNotNull();
    if (actual.getPositions().containsKey(category)) {
      failWithMessage("Expected to be absent in %s queue", category);
    }
    return this;
  }

}
