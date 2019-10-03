package com.rideaustin.assemblers;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.rest.model.SimpleCarDto;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.thirdparty.S3StorageService;

@Component
public class SimpleCarDtoAssembler extends DocumentAwareDtoAssembler<Car, SimpleCarDto> {

  private final S3StorageService s3StorageService;

  @Inject
  public SimpleCarDtoAssembler(DocumentService documentService, S3StorageService s3StorageService) {
    super(documentService);
    this.s3StorageService = s3StorageService;
  }

  @Override
  public SimpleCarDto toDto(Car car, Map<DocumentType, Map<Long, Document>> documents) {
    SimpleCarDto.Builder builder = new SimpleCarDto.Builder();
    Map<DocumentType, DocumentStatus> statusMap = new EnumMap<>(DocumentType.class);
    for (DocumentType type : DocumentType.CAR_PHOTO) {
      Map<Long, Document> statuses = documents.get(type);
      if (statuses != null && statuses.get(car.getId()) != null) {
        statusMap.put(type, statuses.get(car.getId()).getDocumentStatus());
      }
    }
    builder = builder
      .id(car.getId())
      .carPhotosStatus(statusMap)
      .categories(car.getCarCategories())
      .color(car.getColor())
      .driverId(car.getDriver().getId())
      .inspectionStatus(car.getInspectionStatus());
    final String insuranceUrl;
    Map<Long, Document> documentMap = documents.get(DocumentType.INSURANCE);
    if (documentMap == null) {
      insuranceUrl = car.getDriver().getInsurancePictureUrl();
    } else {
      Document insurance = documentMap.get(car.getId());
      insuranceUrl = Optional.ofNullable(insurance).map(Document::getDocumentUrl).orElse(null);
    }
    builder = builder
      .insurancePhotoUrl(StringUtils.isNotBlank(insuranceUrl) ? s3StorageService.getSignedURL(insuranceUrl) : null)
      .insuranceStatus(getDocumentStatus(car, documents, DocumentType.INSURANCE))
      .inspectionStickerStatus(getDocumentStatus(car, documents, DocumentType.CAR_STICKER))
      .license(car.getLicense())
      .make(car.getMake())
      .model(car.getModel())
      .year(car.getYear());
    return builder.build();
  }

  @Override
  public List<SimpleCarDto> toDto(Iterable<Car> cars) {
    Map<DocumentType, Map<Long, Document>> documents = documentService.findCarsDocuments(
      ImmutableList.copyOf(cars),
      DocumentType.CAR_DOCUMENTS
    );
    return StreamSupport.stream(cars.spliterator(), false).map(car -> toDto(car, documents)).collect(Collectors.toList());
  }
}