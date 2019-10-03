package com.rideaustin.service.surgepricing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import com.rideaustin.model.GeolocationLog;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.ActiveDriverInfo;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesUtils;

class SurgeTestSetup {

  private static final double BASE_LAT = 30.2704;
  private static final double BASE_LNG = -97.7423;

  static List<GeolocationLog> setupGeolog() {
    List<GeolocationLog> result = new ArrayList<>();
    List<String> categories = Arrays.asList("REGULAR", "SUV", "PREMIUM");
    Random random = new Random();
    for (int i = 0; i < 830; i++) {
      String category = categories.get(random.nextInt(categories.size()));
      CarType carType = CarTypesUtils.getCarType(category);
      Objects.requireNonNull(carType, "no car type for category:" + category);
      result.add(GeolocationLog.builder().locationLat(BASE_LAT).locationLng(BASE_LNG).carType(carType).build());
    }
    return result;
  }

  static List<OnlineDriverDto> setupAvailableDrivers() {
    List<OnlineDriverDto> result = new ArrayList<>();
    int base = 0;
    for (int i = 0; i < 30; i++, base++) {
      OnlineDriverDto driver = createDriver(base, BASE_LAT + ((double) base / 100000), ActiveDriverStatus.AVAILABLE);
      result.add(driver);
    }
    return result;
  }

  static List<OnlineDriverDto> setupRidingDrivers() {
    List<OnlineDriverDto> result = new ArrayList<>();
    int base = 0;
    for (int i = 0; i < 70; i++, base++) {
      OnlineDriverDto driver = createDriver(base, BASE_LAT + ((double) base / 100000), ActiveDriverStatus.RIDING);
      result.add(driver);
    }
    return result;
  }

  static OnlineDriverDto createDriver(long id, double lat, ActiveDriverStatus status) {
    Driver d = new Driver();
    d.setUser(new User());
    OnlineDriverDto driver = new OnlineDriverDto(new ActiveDriverInfo(id, d, new Car(), 1L));
    driver.setLocationObject(LocationObject.builder().longitude(BASE_LNG).latitude(lat).build());
    driver.setAvailableCarCategoriesBitmask(23);
    driver.setStatus(status);
    return driver;
  }

}
