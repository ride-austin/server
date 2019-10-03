package com.rideaustin.assemblers;

import org.springframework.stereotype.Component;

import com.rideaustin.Constants;
import com.rideaustin.model.DriverEmailHistoryItem;
import com.rideaustin.model.user.DriverEmailReminder;
import com.rideaustin.rest.model.DriverEmailHistoryItemDto;
import com.rideaustin.utils.DateUtils;

@Component
public class DriverEmailHistoryItemDtoAssembler implements SingleSideAssembler<DriverEmailHistoryItem, DriverEmailHistoryItemDto> {

  @Override
  public DriverEmailHistoryItemDto toDto(DriverEmailHistoryItem historyItem) {
    if (historyItem == null) {
      return null;
    }
    String date = Constants.DATETIME_FORMATTER.format(DateUtils.dateToInstant(historyItem.getCreatedDate()));
    DriverEmailReminder reminder = historyItem.getReminder();
    return new DriverEmailHistoryItemDto(historyItem.getId(), date, historyItem.getActor(), reminder.getName(),
      reminder.getId());
  }
}
