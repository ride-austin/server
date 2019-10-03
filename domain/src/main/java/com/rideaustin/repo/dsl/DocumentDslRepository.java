package com.rideaustin.repo.dsl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.rideaustin.config.CacheConfiguration;
import com.rideaustin.model.Document;
import com.rideaustin.model.DocumentDto;
import com.rideaustin.model.QAvatarDocument;
import com.rideaustin.model.QCarDocument;
import com.rideaustin.model.QDocument;
import com.rideaustin.model.QDocumentDto;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.ride.QCar;
import com.rideaustin.model.user.Avatar;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.QDriver;
import com.rideaustin.rest.model.ListAvatarDocumentsParams;
import com.rideaustin.rest.model.ListCarDocumentsParams;

@Repository
public class DocumentDslRepository extends AbstractDocumentDslRepository {

  private static final QAvatarDocument qAvatarDocument = QAvatarDocument.avatarDocument;
  private static final QDocument qDocument = QDocument.document;
  private static final QCarDocument qCarDocument = QCarDocument.carDocument;
  private static final QDriver qDriver = QDriver.driver;
  private static final QCar qCar = QCar.car;

  public Document findOne(Long id) {
    return get(id, Document.class);
  }

  @Cacheable(cacheNames = CacheConfiguration.DOCUMENTS_CACHE, keyGenerator = CacheConfiguration.DOCUMENT_CACHE_KEY_GENERATOR)
  public Document findByAvatarAndType(Long avatarId, DocumentType type) {
    return queryFactory.select(qAvatarDocument.document).from(qAvatarDocument)
      .where(
        qAvatarDocument.avatar.id.eq(avatarId),
        qAvatarDocument.document.documentType.eq(type),
        qAvatarDocument.document.removed.isFalse()
      )
      .orderBy(qAvatarDocument.document.id.desc())
      .fetchFirst();
  }

  @Cacheable(cacheNames = CacheConfiguration.DOCUMENTS_CACHE, keyGenerator = CacheConfiguration.DOCUMENT_CACHE_KEY_GENERATOR)
  public Document findByAvatarAndType(Avatar avatar, DocumentType type) {
    return queryFactory.select(qAvatarDocument.document).from(qAvatarDocument)
      .where(
        qAvatarDocument.avatar.eq(avatar),
        qAvatarDocument.document.documentType.eq(type),
        qAvatarDocument.document.removed.isFalse()
      )
      .orderBy(qAvatarDocument.document.id.desc())
      .fetchFirst();
  }

  public Driver findDriver(Document document) {
    if (DocumentType.DRIVER_DOCUMENTS.contains(document.getDocumentType())) {
      Long avatarId = queryFactory.select(qAvatarDocument.avatar.id).from(qAvatarDocument)
        .where(qAvatarDocument.document.eq(document))
        .fetchOne();
      if (avatarId == null) {
        return null;
      }
      return buildQuery(qDriver)
        .where(qDriver.id.eq(avatarId))
        .fetchOne();
    } else {
      return findCar(document, false).getDriver();
    }
  }

  public Car findCar(Document document) {
    return findCar(document, true);
  }

  public Map<DocumentType, Map<Long, Document>> findDocumentsByAvatarsAndTypes(Iterable<Driver> drivers, Collection<DocumentType> types) {
    List<Pair<Long, Document>> data = buildQuery(qAvatarDocument)
      .where(
        qAvatarDocument.avatar.in(ImmutableList.copyOf(drivers)),
        qAvatarDocument.document.documentType.in(types),
        qAvatarDocument.document.removed.isFalse()
      )
      .fetch()
      .stream()
      .map(avatarDocument -> ImmutablePair.of(avatarDocument.getAvatar().getId(), avatarDocument.getDocument()))
      .collect(Collectors.toList());

    return AbstractDocumentDslRepository.convertDocumentResult(data, types);
  }

  public long getDriversCountWithoutCarPhotos(Long cityId) {
    return queryFactory.select(qCarDocument.car.driver.id).from(qCarDocument)
      .where(
        qCarDocument.car.removed.isFalse(),
        qCarDocument.document.removed.isFalse(),
        qCarDocument.document.documentType.in(DocumentType.CAR_PHOTO),
        qCarDocument.car.driver.cityId.eq(cityId)
      )
      .groupBy(qCarDocument.car)
      .having(qCarDocument.car.count().lt(4))
      .distinct()
      .fetch()
      .size();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public List<Long> findDocumentsIdsToExpireToday() {
    return queryFactory.select(qDocument.id).from(qDocument)
      .where(Expressions.dateOperation(Date.class, Ops.DateTimeOps.DATE, qDocument.validityDate).loe(new Date()),
        qDocument.documentStatus.ne(DocumentStatus.EXPIRED))
      .fetch();

  }

  public List<DocumentDto> findAvatarDocuments(ListAvatarDocumentsParams listAvatarDocumentsParams) {
    BooleanBuilder builder = new BooleanBuilder();
    listAvatarDocumentsParams.fill(builder);
    builder.and(qAvatarDocument.document.removed.isFalse());
    return queryFactory.select(
      new QDocumentDto(qDocument.id, qDocument.documentType, qDocument.documentStatus, qDocument.documentUrl,
        qDocument.name, qDocument.notes, qDocument.cityId, qDocument.validityDate)
    ).from(qAvatarDocument)
      .where(builder)
      .orderBy(qAvatarDocument.id.asc())
      .fetch();
  }

  public List<DocumentDto> findCarDocuments(ListCarDocumentsParams listCarDocumentsParams) {
    BooleanBuilder builder = new BooleanBuilder();
    listCarDocumentsParams.fill(builder);
    builder.and(qDocument.removed.isFalse());
    return queryFactory.select(
      new QDocumentDto(qDocument.id, qDocument.documentType, qDocument.documentStatus, qDocument.documentUrl,
        qDocument.name, qDocument.notes, qDocument.cityId, qDocument.validityDate)
    ).from(qCarDocument)
      .join(qCarDocument.car, qCar)
      .join(qCar.driver, qDriver)
      .join(qCarDocument.document, qDocument)
      .where(builder)
      .orderBy(qCarDocument.id.asc())
      .fetch();
  }

  public List<Driver> findDriversWithExpiredLicenses(Date expirationLimit) {
    JPQLQuery<Long> driverIds = JPAExpressions.select(qAvatarDocument.avatar.id).from(qAvatarDocument)
      .where(
        qAvatarDocument.document.validityDate.before(expirationLimit),
        qAvatarDocument.document.removed.isFalse(),
        qAvatarDocument.document.documentType.eq(DocumentType.LICENSE)
      );
    return buildQuery(qDriver)
      .where(qDriver.id.in(driverIds))
      .fetch();
  }

  private Car findCar(Document document, boolean omitRemoved) {
    BooleanExpression condition = qCarDocument.document.eq(document);
    if (omitRemoved) {
      condition = condition.and(qCarDocument.car.removed.isFalse());
    }
    return queryFactory.select(qCarDocument.car).from(qCarDocument)
      .where(condition)
      .fetchOne();
  }

  @Transactional
  public void setRemoved(Set<Long> ids) {
    queryFactory.update(qDocument)
      .set(qDocument.removed, true)
      .where(qDocument.id.in(ids))
      .execute();
  }
}
