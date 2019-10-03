package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;

import com.google.common.collect.Sets;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.RideTrackingShareDto;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.user.CarTypesCache;

public class RideTrackingShareDtoAssemblerTest {

  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private DocumentService documentService;
  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private Environment environment;

  @InjectMocks
  private RideTrackingShareDtoAssembler assembler;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldSetProperCar() {
    // given
    int bitmask = 123;
    Car expectedCar = Car.builder().selected(true).license("license").carCategoriesBitmask(bitmask).build();
    expectedCar.setId(2L);
    Driver driver = createDriver(Sets.newHashSet(newCar(1L), expectedCar, newCar(3L)));
    Set<String> driverCarTypes = Sets.newHashSet("some type");
    when(carTypesCache.fromBitMask(bitmask)).thenReturn(driverCarTypes);
    when(documentService.findAvatarDocument(any(Driver.class), eq(DocumentType.DRIVER_PHOTO))).thenReturn(new Document());

    // when
    RideTrackingShareDto result = assembler.toDto(createRide(driver, createRider()));

    // then
    assertEquals(expectedCar, result.getDriverCar());
    assertEquals(expectedCar.getLicense(), result.getDriverLicensePlate());
    assertEquals(driverCarTypes, result.getDriverCarTypes());
  }

  @Test
  public void shouldNotSetCarIfNoSelectedCar() {
    // given
    Driver driver = createDriver(Sets.newHashSet(newCar(1L), newCar(3L)));

    // when
    RideTrackingShareDto result = assembler.toDto(createRide(driver, createRider()));

    // then
    assertEquals(null, result.getDriverCar());
    assertEquals(null, result.getDriverLicensePlate());
    assertEquals(null, result.getDriverCarTypes());
  }

  @Test
  public void shouldNotSetCarWithoutActiveDriver() {
    // given
    Ride ride = Ride.builder().requestedCarType(new CarType()).rider(createRider()).build();
    ride.setCreatedDate(new Date());
    ride.setUpdatedDate(new Date());

    // when
    RideTrackingShareDto result = assembler.toDto(ride);

    // then
    assertEquals(null, result.getDriverCar());
    assertEquals(null, result.getDriverLicensePlate());
    assertEquals(null, result.getDriverCarTypes());
  }

  private Driver createDriver(HashSet<Car> cars) {
    Driver driver = Driver.builder().cars(cars).build();
    driver.setUser(new User());
    return driver;
  }

  private Rider createRider() {
    Rider rider = Rider.builder().build();
    rider.setUser(new User());
    return rider;
  }

  private Ride createRide(Driver driver, Rider rider) {
    Ride ride = Ride.builder().activeDriver(ActiveDriver.builder().driver(driver).build()).requestedCarType(new CarType()).rider(rider).build();
    ride.setCreatedDate(new Date());
    ride.setUpdatedDate(new Date());
    return ride;
  }

  private Car newCar(long id) {
    Car car = new Car();
    car.setId(id);
    return car;
  }
}