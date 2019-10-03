package com.rideaustin.repo.dsl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.DriverEmailHistoryItem;
import com.rideaustin.model.QDriverEmailHistoryItem;
import com.rideaustin.model.user.DriverEmailReminder;
import com.rideaustin.model.user.QDriverEmailReminder;
import com.rideaustin.rest.exception.NotFoundException;

@Repository
public class DriverEmailReminderDslRepository extends AbstractDslRepository {

  private static final QDriverEmailReminder qReminder = QDriverEmailReminder.driverEmailReminder;
  private static final QDriverEmailHistoryItem qHistoryItem = QDriverEmailHistoryItem.driverEmailHistoryItem;

  public List<DriverEmailReminder> listReminders() {
    return buildQuery(qReminder).orderBy(qReminder.name.asc()).fetch();
  }

  public DriverEmailReminder findOne(long id) throws NotFoundException {
    DriverEmailReminder driverEmailReminder = get(id, DriverEmailReminder.class);
    return Optional.ofNullable(driverEmailReminder).orElseThrow(() -> new NotFoundException(String.format("Reminder with id %d not found", id)));
  }

  public List<DriverEmailHistoryItem> findHistory(long driverId) {
    return buildQuery(qHistoryItem)
      .where(qHistoryItem.driverId.eq(driverId))
      .orderBy(qHistoryItem.createdDate.asc())
      .fetch();
  }

  public DriverEmailHistoryItem findHistoryItem(long historyItemId) {
    return buildQuery(qHistoryItem)
      .where(qHistoryItem.id.eq(historyItemId))
      .fetchOne();
  }

  public DriverEmailReminder findActivationEmail(Long cityId) {
    return buildQuery(qReminder)
      .where(
        qReminder.cityId.eq(cityId),
        qReminder.name.eq("Activation email")
        )
      .fetchOne();
  }
}
