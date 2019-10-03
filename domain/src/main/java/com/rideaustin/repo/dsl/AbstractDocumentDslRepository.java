package com.rideaustin.repo.dsl;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;

public class AbstractDocumentDslRepository extends AbstractDslRepository {

  protected static Map<DocumentType, Map<Long, Document>> convertDocumentResult(List<Pair<Long, Document>> pairs, Collection<DocumentType> requiredTypes) {
    Map<Long, Map<DocumentType, Document>> dataPerItem = new HashMap<>();
    for (Pair<Long, Document> pair : pairs) {
      Map<DocumentType, Document> itemDocumentMap = dataPerItem.get(pair.getLeft());
      Document newDocument = pair.getRight();
      DocumentType type = newDocument.getDocumentType();
      if (itemDocumentMap != null) {
        Document document = itemDocumentMap.get(type);
        if (document == null || document.getId() < newDocument.getId()) {
          itemDocumentMap.put(type, newDocument);
        }
      } else {
        Map<DocumentType, Document> newMap = new EnumMap<>(DocumentType.class);
        newMap.put(type, newDocument);
        dataPerItem.put(pair.getLeft(), newMap);
      }
    }

    fillNotProvidedDocuments(requiredTypes, dataPerItem);

    return constructResult(dataPerItem);
  }

  private static Map<DocumentType, Map<Long, Document>> constructResult(Map<Long, Map<DocumentType, Document>> dataPerItem) {
    Map<DocumentType, Map<Long, Document>> result = new EnumMap<>(DocumentType.class);
    for (Map.Entry<Long, Map<DocumentType, Document>> entry : dataPerItem.entrySet()) {
      for (Map.Entry<DocumentType, Document> docEntry : entry.getValue().entrySet()) {
        if (result.get(docEntry.getKey()) != null) {
          result.get(docEntry.getKey()).put(entry.getKey(), docEntry.getValue());
        } else {
          HashMap<Long, Document> newMap = new HashMap<>();
          newMap.put(entry.getKey(), docEntry.getValue());
          result.put(docEntry.getKey(), newMap);
        }
      }
    }
    return result;
  }

  private static void fillNotProvidedDocuments(Collection<DocumentType> requiredTypes, Map<Long, Map<DocumentType, Document>> dataPerItem) {
    for (Map<DocumentType, Document> itemDocumentMap : dataPerItem.values()) {
      for (DocumentType requiredType : requiredTypes) {
        if (!itemDocumentMap.containsKey(requiredType)) {
          Document mockDocument = new Document();
          mockDocument.setDocumentStatus(DocumentStatus.NOT_PROVIDED);
          itemDocumentMap.put(requiredType, mockDocument);
        }
      }
    }
  }
}
