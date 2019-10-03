package com.rideaustin.assemblers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.SimpleDriverDto;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.DriverTypeUtils;
import com.rideaustin.utils.DriverUtils;

@Component
public class SimpleDriverDtoAssembler extends DocumentAwareDtoAssembler<Driver, SimpleDriverDto> {

  private final S3StorageService s3StorageService;
  private final SimpleCarDtoAssembler carDtoAssembler;

  @Inject
  public SimpleDriverDtoAssembler(S3StorageService s3StorageService, DocumentService documentService, SimpleCarDtoAssembler carDtoAssembler) {
    super(documentService);
    this.s3StorageService = s3StorageService;
    this.carDtoAssembler = carDtoAssembler;
  }

  @Override
  public SimpleDriverDto toDto(Driver driver, Map<DocumentType, Map<Long, Document>> documents) {
    List<Car> cars = DriverUtils.getActiveCars(driver);
    return SimpleDriverDto.builder().active(driver.isActive())
      .cars(carDtoAssembler.toDto(cars))
      .cityApprovalStatus(driver.getCityApprovalStatus())
      .activationStatus(driver.getActivationStatus())
      .driverId(driver.getId())
      .driverLicensePicture(Optional.ofNullable(getDocumentUrl(driver, documents, DocumentType.LICENSE)).map(s3StorageService::getSignedURL).orElse(null))
      .driverLicenseStatus(getDocumentStatus(driver, documents, DocumentType.LICENSE))
      .driverPicture(getDocumentUrl(driver, documents, DocumentType.DRIVER_PHOTO))
      .driverTypes(Optional.ofNullable(driver.getGrantedDriverTypesBitmask()).map(DriverTypeUtils::fromBitMask).orElse(Collections.emptySet()))
      .email(driver.getEmail())
      .enabled(driver.getUser().getUserEnabled())
      .firstName(driver.getFirstname())
      .gender(driver.getGender())
      .lastName(driver.getLastname())
      .payoneerStatus(driver.getPayoneerStatus())
      .phoneNumber(driver.getPhoneNumber())
      .profilePhotosStatus(getDocumentStatus(driver, documents, DocumentType.DRIVER_PHOTO))
      .rating(driver.getRating())
      .userId(driver.getUser().getId())
      .onboardingStatus(driver.getOnboardingStatus())
      .userPicture(driver.getUser().getPhotoUrl())
      .lastLoginDate(driver.getLastLoginDate())
      .build();
  }

  @Override
  public List<SimpleDriverDto> toDto(Iterable<Driver> drivers) {
    return toDto(drivers,
      documentService.findAvatarsDocuments(drivers, DocumentType.DRIVER_DOCUMENTS));
  }

  public List<SimpleDriverDto> toDto(Iterable<Driver> drivers, Map<DocumentType, Map<Long, Document>> documents) {
    return StreamSupport.stream(drivers.spliterator(), false)
      .map(d -> toDto(d, documents))
      .collect(Collectors.toList());
  }

  public Page<SimpleDriverDto> toPageDto(Page<Driver> pageDs, Pageable pageable) {
    List<SimpleDriverDto> ts = toDto(pageDs.getContent());
    return new PageImpl<>(ts, pageable, pageDs.getTotalElements());
  }
}
