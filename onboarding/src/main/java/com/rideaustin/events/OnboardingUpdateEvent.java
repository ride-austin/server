package com.rideaustin.events;

import java.util.List;

import com.rideaustin.model.Document;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class OnboardingUpdateEvent<T> {

  private final T old;
  private final T updated;
  private final Long driverId;
  private Context context;

  @RequiredArgsConstructor
  @Getter
  public static class Context {
    final Driver driver;
    final List<Car> cars;
    final List<Document> documents;
  }

  public void fillContext(Driver driver, List<Car> cars, List<Document> documents) {
    this.context = new Context(driver, cars, documents);
  }
}
