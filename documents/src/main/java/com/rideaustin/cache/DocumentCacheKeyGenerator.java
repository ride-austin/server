package com.rideaustin.cache;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Avatar;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Component
public class DocumentCacheKeyGenerator implements KeyGenerator {

  @Override
  public Object generate(Object target, Method method, Object... params) {
    BaseEntity subject = null;
    Document document = null;
    DocumentType type = null;
    Long subjectId = null;
    for (Object param : params) {
      if (param instanceof Car || param instanceof Avatar) {
        subject = (BaseEntity) param;
      } else if (param instanceof Document) {
        document = (Document) param;
      } else if (param instanceof DocumentType) {
        type = (DocumentType) param;
      } else if (param instanceof Long) {
        subjectId = (Long) param;
      }
    }
    if (subject == null && subjectId == null) {
      //should never happen
      return new DocumentKey(null, 0);
    } else if (subject != null){
      return resolveKey(document, type, subject.getId());
    } else {
      return resolveKey(document, type, subjectId);
    }
  }

  private Object resolveKey(Document document, DocumentType type, long subjectId) {
    if (document == null && type == null) {
      return new DocumentKey(null, subjectId);
    } else if (document == null) {
      return new DocumentKey(type, subjectId);
    } else if (type == null) {
      return new DocumentKey(document.getDocumentType(), subjectId);
    } else {
      return new DocumentKey(type, subjectId);
    }
  }

  @Getter
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class DocumentKey implements Serializable {
    private final DocumentType type;
    private final long ownerId;
  }
}
