package com.rideaustin.repo.dsl;

import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableList;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.rideaustin.model.QAvatarDocument;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.user.QDriver;

@Repository
public class AvatarDocumentDslRepository extends AbstractDocumentDslRepository {

  private static final QDriver qDriver = QDriver.driver;
  private static final QAvatarDocument qAvatarDocument = QAvatarDocument.avatarDocument;

  public Long findOwnerIdByDocumentId(Long documentId) {
    return queryFactory.select(qAvatarDocument.avatar.id)
      .from(qAvatarDocument)
      .where(qAvatarDocument.document.id.eq(documentId)).fetchOne();
  }

  public long getDriversWithoutProfilePhotosCount(Long cityId) {
    JPQLQuery<Long> driversWithPhotos = JPAExpressions.select(qAvatarDocument.avatar.id).from(qAvatarDocument)
      .where(
        qAvatarDocument.document.documentType.eq(DocumentType.DRIVER_PHOTO),
        qAvatarDocument.document.removed.isFalse(),
        qAvatarDocument.document.documentStatus.notIn(ImmutableList.of(DocumentStatus.PENDING, DocumentStatus.REJECTED))
      );
    return buildQuery(qDriver)
      .where(
        qDriver.id.notIn(driversWithPhotos),
        qDriver.cityId.eq(cityId)
      )
      .fetchCount();
  }

}
