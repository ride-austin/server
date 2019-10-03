package com.rideaustin.test.fixtures;

import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.rideaustin.test.fixtures.check.NoOpChecker;
import com.rideaustin.test.fixtures.check.RecordChecker;

public abstract class AbstractFixture<T> {

  protected EntityManager entityManager;
  protected boolean fixed;
  protected RecordChecker<T> recordChecker = new NoOpChecker<>();

  public T getFixture() {
    T object = createObject();
    Optional<T> existing = recordChecker.getIfExists(object);
    if (existing.isPresent()) {
      return existing.get();
    }

    if (object instanceof Collection) {
      Collection collection = (Collection) object;
      for (Object o : collection) {
        entityManager.merge(o);
      }
    } else {
      object = entityManager.merge(object);
    }
    entityManager.flush();
    fixed = true;
    return object;
  }

  protected abstract T createObject();

  @Inject
  public void setEntityManager(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public void setRecordChecker(RecordChecker<T> recordChecker) {
    this.recordChecker = recordChecker;
  }

  public boolean isFixed() {
    return fixed;
  }
}
