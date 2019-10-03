package com.rideaustin.repo.dsl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;

import com.rideaustin.model.Event;
import com.rideaustin.model.QEvent;
import com.rideaustin.model.enums.AvatarType;

@Repository
public class EventDslRepository extends AbstractDslRepository {

  private static final QEvent qEvent = QEvent.event;

  public List<Event> listEvents(Set<Long> ids, AvatarType avatarType) {
    return buildQuery(qEvent)
      .where(qEvent.avatarId.in(ids).
        and(qEvent.avatarType.eq(avatarType)))
      .fetch();
  }

  public void deleteInBatch(List<Event> entities) {
    entities.forEach(entityManager::remove);
    entityManager.flush();
  }

  @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(delay = 1000))
  public void deleteExpiredEvents(Date currentDate) {
    queryFactory.delete(qEvent).where(qEvent.expiresOn.before(currentDate)).execute();
  }
}
