package com.rideaustin.repo.dsl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.rideaustin.config.CacheConfiguration;
import com.rideaustin.model.CarDocument;
import com.rideaustin.model.Document;
import com.rideaustin.model.QCarDocument;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;

@Repository
public class CarDocumentDslRepository extends AbstractDocumentDslRepository {

  private static final QCarDocument qCarDocument = QCarDocument.carDocument;

  public List<Document> findCarPhotos(Long carId) {
    return queryFactory.select(qCarDocument.document).from(qCarDocument)
      .where(
        qCarDocument.car.id.eq(carId),
        qCarDocument.document.documentType.in(DocumentType.CAR_PHOTO),
        qCarDocument.document.removed.isFalse()
      )
      .fetch();
  }

  public List<Document> findCarsDocuments(Collection<Car> cars) {
    return findDocumentsByCarsAndTypes(cars, DocumentType.CAR_DOCUMENTS)
      .values()
      .stream()
      .map(Map::values)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  public CarDocument findByDocumentId(Long documentId) {
    return buildQuery(qCarDocument).where(qCarDocument.document.id.eq(documentId)).fetchOne();
  }

  @Cacheable(cacheNames = CacheConfiguration.DOCUMENTS_CACHE, keyGenerator = CacheConfiguration.DOCUMENT_CACHE_KEY_GENERATOR)
  public Document findByCarAndType(Long carId, DocumentType type) {
    return queryFactory.select(qCarDocument.document).from(qCarDocument)
      .where(
        qCarDocument.car.id.eq(carId),
        qCarDocument.document.documentType.eq(type),
        qCarDocument.document.removed.isFalse()
      )
      .orderBy(qCarDocument.document.id.desc())
      .fetchFirst();
  }

  public Map<DocumentType, Map<Long, Document>> findDocumentsByCarsAndTypes(Collection<Car> cars, Collection<DocumentType> types) {
    List<Pair<Long, Document>> carDocuments = buildQuery(qCarDocument)
      .where(
        qCarDocument.car.in(cars),
        qCarDocument.car.removed.isFalse(),
        qCarDocument.document.documentType.in(types),
        qCarDocument.document.removed.isFalse()
      )
      .fetch()
      .stream()
      .map(carDocument -> ImmutablePair.of(carDocument.getCar().getId(), carDocument.getDocument()))
      .collect(Collectors.toList());
    return convertDocumentResult(carDocuments, types);
  }

  public Driver findOwnerByDocumentId(Long documentId) {
    return queryFactory.select(qCarDocument.car.driver).from(qCarDocument).where(qCarDocument.document.id.eq(documentId)).fetchOne();
  }
}
