package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.user.DriverEmailReminder;
import com.rideaustin.rest.model.DriverEmailReminderDto;
import com.rideaustin.rest.model.DriverEmailReminderDto.ExtraField;
import com.rideaustin.rest.model.DriverEmailReminderDto.ExtraField.ExtraFieldType;

public class DriverEmailReminderDtoAssemblerTest {

  private DriverEmailReminderDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new DriverEmailReminderDtoAssembler();
  }

  @Test
  public void toDtoSkipsNull() {
    final DriverEmailReminderDto result = testedInstance.toDto((DriverEmailReminder) null);

    assertNull(result);
  }

  @Test
  public void toDtoFillsInfo() {
    final DriverEmailReminder reminder = new DriverEmailReminder();
    reminder.setId(1L);
    reminder.setName("A");
    reminder.setCityId(1L);

    final DriverEmailReminderDto result = testedInstance.toDto(reminder);

    assertEquals(reminder.getId(), result.getId());
    assertEquals(reminder.getName(), result.getName());
    assertEquals(reminder.getCityId(), result.getCityId());
    assertTrue(result.getExtraFields().isEmpty());
  }

  @Test
  public void toDtoSetsContent() {
    final DriverEmailReminder reminder = new DriverEmailReminder();
    reminder.setSubject(null);
    reminder.setStoreContent(true);

    final DriverEmailReminderDto result = testedInstance.toDto(reminder);

    assertTrue(CollectionUtils.isEqualCollection(result.getExtraFields(), Collections.singleton(new ExtraField(
      "content", "Content", ExtraFieldType.TEXTAREA, 1
    ))));
  }

  @Test
  public void toDtoSetsSubject() {
    final DriverEmailReminder reminder = new DriverEmailReminder();
    reminder.setSubject("Hello");
    reminder.setStoreContent(false);

    final DriverEmailReminderDto result = testedInstance.toDto(reminder);

    assertTrue(CollectionUtils.isEqualCollection(result.getExtraFields(), Collections.singleton(new ExtraField(
      "subject", "Subject", ExtraFieldType.TEXT, 1
    ))));
  }

  @Test
  public void toDtoSetsExtraFields() {
    final DriverEmailReminder reminder = new DriverEmailReminder();
    reminder.setSubject("Hello");
    reminder.setStoreContent(true);

    final DriverEmailReminderDto result = testedInstance.toDto(reminder);

    assertTrue(CollectionUtils.isEqualCollection(result.getExtraFields(), ImmutableSet.of(new ExtraField(
        "content", "Content", ExtraFieldType.TEXTAREA, 1
      ),
      new ExtraField(
        "subject", "Subject", ExtraFieldType.TEXT, 2
      ))));
  }
}