package com.rideaustin.assemblers;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.rideaustin.model.user.DriverEmailReminder;
import com.rideaustin.rest.model.DriverEmailReminderDto;
import com.rideaustin.rest.model.DriverEmailReminderDto.ExtraField;
import com.rideaustin.rest.model.DriverEmailReminderDto.ExtraField.ExtraFieldType;

@Component
public class DriverEmailReminderDtoAssembler implements SingleSideAssembler<DriverEmailReminder, DriverEmailReminderDto> {
  @Override
  public DriverEmailReminderDto toDto(DriverEmailReminder reminder) {
    if (reminder == null) {
      return null;
    }
    Set<ExtraField> extraFields = new HashSet<>();
    int order = 1;
    if (reminder.isStoreContent()) {
      extraFields.add(new ExtraField("content", "Content", ExtraFieldType.TEXTAREA, order++));
    }
    if (!StringUtils.isEmpty(reminder.getSubject())) {
      extraFields.add(new ExtraField("subject", "Subject", ExtraFieldType.TEXT, order));
    }
    return new DriverEmailReminderDto(reminder.getId(), reminder.getName(), reminder.getCityId(), extraFields);
  }
}
