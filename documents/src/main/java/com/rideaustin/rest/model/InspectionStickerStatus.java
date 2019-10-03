package com.rideaustin.rest.model;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.rideaustin.Constants;
import com.rideaustin.model.QCarDocument;
import com.rideaustin.model.QDocument;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.user.QDriver;

public enum InspectionStickerStatus {
  APPROVED {
    @Override
    protected DocumentStatus status() {
      return DocumentStatus.APPROVED;
    }
  },
  PENDING {
    @Override
    protected DocumentStatus status() {
      return DocumentStatus.PENDING;
    }
  },
  EXPIRED {
    @Override
    protected DocumentStatus status() {
      return DocumentStatus.EXPIRED;
    }
  },
  REJECTED {
    @Override
    protected DocumentStatus status() {
      return DocumentStatus.REJECTED;
    }
  },
  NOT_REQUIRED {
    @Override
    public JPQLQuery<Long> query() {
      QDriver qDriver = QDriver.driver;
      return JPAExpressions.select(qDriver.id).from(qDriver).where(qDriver.cityId.eq(Constants.DEFAULT_CITY_ID));
    }

    @Override
    protected DocumentStatus status() {
      return null;
    }
  };

  public JPQLQuery<Long> query() {
    QCarDocument carDocument = QCarDocument.carDocument;
    QDocument document = carDocument.document;
    return JPAExpressions.select(carDocument.car.driver.id)
      .from(carDocument)
      .where(document.documentType.eq(DocumentType.CAR_STICKER)
        .and(document.documentStatus.eq(status()))
        .and(document.removed.isFalse())
      );
  }

  protected abstract DocumentStatus status();
}
