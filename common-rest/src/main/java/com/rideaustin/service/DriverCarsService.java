package com.rideaustin.service;

import com.rideaustin.assemblers.CarDtoAssembler;
import com.rideaustin.events.OnboardingUpdateEvent;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.repo.dsl.CarDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.CarDto;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.user.CarTypesCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverCarsService {

  private static final String CAR_NOT_FOUND_MESSAGE = "Car not found";

  private final CarDslRepository carDslRepository;
  private final CarDtoAssembler carDtoAssembler;
  private final CarDocumentDslRepository carDocumentDslRepository;
  private final DocumentDslRepository documentDslRepository;
  private final DriverService driverService;
  private final EventsNotificationService notificationService;
  private final CarTypesCache carTypesCache;
  private final ActiveDriversService activeDriversService;
  private final CurrentUserService currentUserService;
  private final DocumentService documentService;
  private final ApplicationEventPublisher publisher;

  public Car addCar(long driverId, @Valid CarDto carDto, @Deprecated MultipartFile photo, MultipartFile insurancePhoto) throws RideAustinException {
    Driver driver = driverService.findDriver(driverId, currentUserService.getUser());
    Car car = carDtoAssembler.toDs(carDto);

    boolean firstCarToAdd = CollectionUtils.isEmpty(driver.getCars());

    car.setDriver(driver);
    car.setRemoved(false);

    if (firstCarToAdd) {
      car.setSelected(true);
    }

    Car savedCar = carDslRepository.save(car);
    if (photo != null) {
      updateCarPhoto(savedCar, photo);
    }
    if (insurancePhoto != null) {
      Document insurance = documentService.uploadDocument(insurancePhoto, DocumentType.INSURANCE, carDto.getInsuranceExpiryDate(), null, driverId);
      documentService.saveCarDocument(car, insurance);
    } else if (firstCarToAdd) {
      Document insurance = documentService.createDocument(DocumentType.INSURANCE, driver.getInsurancePictureUrl(),
        driver.getInsuranceExpiryDate(), null, driverId);
      documentService.saveCarDocument(savedCar, insurance);
    }
    return savedCar;
  }

  public Car editCar(long driverId, long carId, CarDto carDto) throws RideAustinException {
    User user = currentUserService.getUser();
    Driver driver = driverService.findDriver(driverId, user);
    Car currentCar = getCar(carId, driver);
    Car copy = new Car();
    Car car = carDtoAssembler.toDs(carDto);
    BeanUtils.copyProperties(currentCar, copy);
    trackCarChanges(user, currentCar, car);

    if (currentCar.isSelected() &&
      currentCar.getCarCategoriesBitmask() != carTypesCache.toBitMask(car.getCarCategories())) {
      // initiate these actions for 'selected' car only
      notificationService.sendCarCategoryChange(driver.getId());
      activeDriversService.adjustActiveDriverAvailableCarCategories(car, driver);
    }

    Document insurance = carDocumentDslRepository.findByCarAndType(carId, DocumentType.INSURANCE);
    if (insurance != null && !Objects.equals(insurance.getValidityDate(), carDto.getInsuranceExpiryDate())) {
      insurance.setValidityDate(carDto.getInsuranceExpiryDate());
      documentService.updateDocument(insurance.getId(), insurance);
    }

    currentCar.updateByAdmin(car);
    Car savedCar = carDslRepository.save(currentCar);

    publisher.publishEvent(new OnboardingUpdateEvent<>(copy, savedCar, driverId));

    return savedCar;
  }

  private void trackCarChanges(User currentUser, Car oldCar, Car newCar) {
    if (!oldCar.getInspectionStatus().equals(newCar.getInspectionStatus())) {
      log.info("User: " + currentUser.getEmail() + " changed car id:  " + oldCar.getId()
        + "inspection status - old status " + oldCar.getInspectionStatus().name() + " new status " + newCar.getInspectionStatus().name());
    }
  }

  private Car getCar(long id) throws RideAustinException {
    Car car = carDslRepository.findOne(id);
    if (car == null) {
      throw new NotFoundException(CAR_NOT_FOUND_MESSAGE);
    }
    if (car.isRemoved()) {
      throw new BadRequestException("The car has been removed and cannot be edited");
    }
    return car;
  }

  @Nonnull
  private Car getCar(long carId, @Nullable Driver driver) throws RideAustinException {
    Car car = getCar(carId);
    if (!car.getDriver().equals(driver)) {
      throw new NotFoundException(CAR_NOT_FOUND_MESSAGE);
    }
    return car;
  }

  public Car selectDriverCar(Long carToSelectId, Long driverId) throws RideAustinException {
    Driver driver = driverService.findDriver(driverId, currentUserService.getUser());
    Car car = carDslRepository.findOne(carToSelectId);
    if (car == null || !car.getDriver().equals(driver)) {
      throw new NotFoundException(CAR_NOT_FOUND_MESSAGE);
    }
    if (!car.getInspectionStatus().equals(CarInspectionStatus.APPROVED)) {
      throw new BadRequestException("Trying to select unapproved car");
    }
    if (car.isRemoved()) {
      throw new BadRequestException("Trying to select removed car");
    }

    // deselect previously selected
    Car prevSelectedCar = carDslRepository.getSelected(driver);
    if (prevSelectedCar != null) {
      prevSelectedCar.setSelected(false);
      carDslRepository.save(prevSelectedCar);
    }

    // select new
    car.setSelected(true);
    return carDslRepository.save(car);
  }

  public void removeDriverCar(Long carToRemove, Long driverId) throws RideAustinException {
    User user = currentUserService.getUser();
    Driver driver = driverService.findDriver(driverId, user);
    Car car = carDslRepository.findOne(carToRemove);
    if (car == null || !car.getDriver().equals(driver)) {
      throw new NotFoundException(CAR_NOT_FOUND_MESSAGE);
    }
    if (!user.isAdmin()) {
      ActiveDriver activeDriver = activeDriversService.getCurrentActiveDriver();
      if (activeDriver != null && car.equals(activeDriver.getSelectedCar())) {
        throw new BadRequestException("You can't remove your current car while being online. Please go offline to manage your cars");
      }
    } else if (car.isSelected() && DriverActivationStatus.ACTIVE.equals(driver.getActivationStatus()) && CarInspectionStatus.APPROVED.equals(car.getInspectionStatus())) {
      throw new BadRequestException("You can't remove an approved car which is currently selected by an active driver. Please ask driver to add and select another car before deleting this one.");
    }
    car.setSelected(false);
    car.setRemoved(true);
    carDslRepository.save(car);
  }

  private void updateCarPhoto(Car car, MultipartFile photo) throws ServerError {
    if (photo == null) {
      return;
    }
    Document carPhoto = documentService.uploadPublicDocument(photo, DocumentType.CAR_PHOTO_FRONT,
      car.getDriver().getCityId(), car.getDriver().getId());
    Optional<Document> frontPhoto = carDocumentDslRepository.findCarPhotos(car.getId())
      .stream()
      .filter(d -> DocumentType.CAR_PHOTO_FRONT.equals(d.getDocumentType()))
      .findFirst();
    if (frontPhoto.isPresent()) {
      frontPhoto.get().setRemoved(true);
      documentDslRepository.save(frontPhoto.get());
    }

    documentService.saveCarDocument(car, carPhoto);
  }

}

