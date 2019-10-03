package com.rideaustin.rest.model;

import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.QAvatarDocument;
import com.rideaustin.model.QCarDocument;
import com.rideaustin.model.QDocument;
import com.rideaustin.model.user.QDriver;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ListDriversWithDocumentsParams extends ListDriversParams {

  private static final QDriver qDriver = QDriver.driver;
  private static final QAvatarDocument qAvatarDocument = QAvatarDocument.avatarDocument;
  private static final QCarDocument qCarDocument = QCarDocument.carDocument;

  @ApiModelProperty(name = "Driver license status", dataType = "List", allowableValues = "APPROVED,REJECTED,PENDING,EXPIRED")
  private List<DocumentStatus> driverLicenseStatus;
  @ApiModelProperty(name = "Insurance status", dataType = "List", allowableValues = "APPROVED,REJECTED,PENDING,EXPIRED")
  private List<DocumentStatus> insuranceStatus;
  @ApiModelProperty(name = "Car photos status", dataType = "List", allowableValues = "APPROVED,REJECTED,PENDING")
  private List<DocumentStatus> carPhotosStatus;
  @ApiModelProperty(name = "Driver photos status", dataType = "List", allowableValues = "APPROVED,REJECTED,PENDING")
  private List<DocumentStatus> profilePhotosStatus;
  @ApiModelProperty(name = "Inspection sticker status", dataType = "List", allowableValues = "APPROVED,REJECTED,PENDING,EXPIRED,NOT_REQUIRED")
  private List<InspectionStickerStatus> inspectionStickerStatus;

  @Override
  public void fill(BooleanBuilder builder) {
    super.fill(builder);
    if (driverLicenseStatus != null) {
      QDocument document = qAvatarDocument.document;
      JPQLQuery<Long> documentSubQuery = JPAExpressions.select(qAvatarDocument.avatar.id).from(qAvatarDocument)
        .where(document.documentStatus.in(driverLicenseStatus).and(document.documentType.eq(DocumentType.LICENSE)));
      builder.and(qDriver.id.in(documentSubQuery));
    }
    if (insuranceStatus != null) {
      QDocument document = qAvatarDocument.document;
      JPQLQuery<Long> driverSubQuery = JPAExpressions.select(qAvatarDocument.avatar.id).from(qAvatarDocument)
        .where(document.documentStatus.in(insuranceStatus)
          .and(document.documentType.eq(DocumentType.INSURANCE))
          .and(document.removed.isFalse()));
      JPQLQuery<Long> carSubQuery = JPAExpressions.select(qCarDocument.car.driver.id).from(qCarDocument)
        .where(qCarDocument.document.documentStatus.in(insuranceStatus)
          .and(qCarDocument.document.documentType.eq(DocumentType.INSURANCE))
          .and(qCarDocument.document.removed.isFalse()));
      builder.and(qDriver.id.in(driverSubQuery).or(qDriver.id.in(carSubQuery)));
    }
    if (carPhotosStatus != null) {
      QDocument carDocument = qCarDocument.document;
      JPQLQuery<Long> carPhotoSubQuery = JPAExpressions.select(qCarDocument.car.driver.id).from(qCarDocument)
        .where(carDocument.documentStatus.in(carPhotosStatus)
          .and(carDocument.removed.isFalse())
          .and(carDocument.documentType.in(DocumentType.CAR_PHOTO))
        );

      BooleanExpression predicate = qDriver.id.in(carPhotoSubQuery);
      if (carPhotosStatus.contains(DocumentStatus.NOT_PROVIDED) || carPhotosStatus.contains(DocumentStatus.REJECTED)) {
        JPQLQuery<Long> driversWithoutCarPhotos = JPAExpressions.select(qCarDocument.car.driver.id).from(qCarDocument)
          .where(
            qCarDocument.car.removed.isFalse(),
            qCarDocument.document.removed.isFalse(),
            qCarDocument.document.documentType.in(DocumentType.CAR_PHOTO),
            qCarDocument.document.documentStatus.in(carPhotosStatus)
          )
          .groupBy(qCarDocument.car)
          .having(qCarDocument.car.count().lt(4))
          .distinct();

        predicate.or(qDriver.id.in(driversWithoutCarPhotos)).or(predicate);

      }
      builder.and(predicate);
    }
    if (profilePhotosStatus != null) {
      QDocument document = qAvatarDocument.document;
      JPQLQuery<Long> profilePhotoSubQuery = JPAExpressions.select(qAvatarDocument.avatar.id).from(qAvatarDocument)
        .where(document.documentStatus.in(profilePhotosStatus)
          .and(document.removed.isFalse())
          .and(document.documentType.eq(DocumentType.DRIVER_PHOTO)));
      BooleanExpression predicate = qDriver.id.in(profilePhotoSubQuery);
      if (profilePhotosStatus.contains(DocumentStatus.PENDING) || profilePhotosStatus.contains(DocumentStatus.REJECTED)) {
        JPQLQuery<Long> driversWithPhotos = JPAExpressions.select(qAvatarDocument.avatar.id).from(qAvatarDocument)
          .where(
            qAvatarDocument.document.documentType.eq(DocumentType.DRIVER_PHOTO),
            qAvatarDocument.document.removed.isFalse(),
            qAvatarDocument.document.documentStatus.notIn(profilePhotosStatus)
          );
        predicate = predicate.or(qDriver.id.notIn(driversWithPhotos));
        builder.and(qDriver.id.notIn(driversWithPhotos));
      }
      builder.and(predicate);
    }
    if (inspectionStickerStatus != null) {
      BooleanBuilder stickerBuilder = new BooleanBuilder();
      for (InspectionStickerStatus status : inspectionStickerStatus) {
        stickerBuilder.or(qDriver.id.in(status.query()));
      }
      builder.and(stickerBuilder);
    }
  }
}
