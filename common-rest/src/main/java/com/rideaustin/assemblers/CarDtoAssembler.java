package com.rideaustin.assemblers;

import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.rest.model.CarDto;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.CarTypesUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CarDtoAssembler implements BilateralAssembler<Car, CarDto> {

  private final S3StorageService s3StorageService;
  private final CarDocumentDslRepository carDocumentDslRepository;

  @Override
  public Car toDs(CarDto dto) {
    if (dto == null) {
      return null;
    }
    Car car = new Car();
    car.setId(dto.getId());
    car.setInspectionStatus(Optional.ofNullable(dto.getInspectionStatus()).orElse(CarInspectionStatus.NOT_INSPECTED));
    if (CollectionUtils.isNotEmpty(dto.getCarCategories())) {
      car.setCarCategoriesBitmask(CarTypesUtils.toBitMask(dto.getCarCategories()));
      car.setCarCategories(dto.getCarCategories());
    }
    car.setColor(dto.getColor());
    car.setInspectionNotes(dto.getInspectionNotes());
    car.setLicense(dto.getLicense());
    car.setMake(dto.getMake());
    car.setModel(dto.getModel());
    car.setYear(dto.getYear());
    if (dto.getSelected() != null) {
      car.setSelected(dto.getSelected());
    }
    if (dto.getRemoved() != null) {
      car.setRemoved(dto.getRemoved());
    }
    return car;
  }

  @Override
  public CarDto toDto(Car car) {
    if (car == null) {
      return null;
    }
    Document insurance = carDocumentDslRepository.findByCarAndType(car.getId(), DocumentType.INSURANCE);
    String insuranceUrl = Optional.ofNullable(insurance)
      .map(Document::getDocumentUrl)
      .map(s3StorageService::getSignedURL)
      .orElse(null);
    Date insuranceExpiryDate = Optional.ofNullable(insurance)
      .map(Document::getValidityDate)
      .orElse(null);
    Document photo = carDocumentDslRepository.findByCarAndType(car.getId(), DocumentType.CAR_PHOTO_FRONT);
    String photoUrl = Optional.ofNullable(photo)
      .map(Document::getDocumentUrl)
      .orElse(null);
    return new CarDto(car.getId(), car.getColor(), car.getLicense(), car.getMake(), car.getModel(), car.getYear(),
      insuranceExpiryDate, insuranceUrl, car.isSelected(), car.getInspectionStatus(), car.getInspectionNotes(),
      car.isRemoved(), CarTypesUtils.fromBitMask(car.getCarCategoriesBitmask()), photoUrl);
  }
}
