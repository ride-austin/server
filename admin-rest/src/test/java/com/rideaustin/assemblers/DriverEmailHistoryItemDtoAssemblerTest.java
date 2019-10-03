package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.DriverEmailHistoryItem;
import com.rideaustin.model.user.DriverEmailReminder;
import com.rideaustin.rest.model.DriverEmailHistoryItemDto;

public class DriverEmailHistoryItemDtoAssemblerTest {

  private DriverEmailHistoryItemDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new DriverEmailHistoryItemDtoAssembler();
  }

  @Test
  public void toDtoSkipsNull() {
    final DriverEmailHistoryItemDto result = testedInstance.toDto((DriverEmailHistoryItem) null);

    assertNull(result);
  }

  @Test
  public void toDtoFillsInfo() {
    final DriverEmailHistoryItem source = new DriverEmailHistoryItem();
    final DriverEmailReminder reminder = new DriverEmailReminder();
    source.setId(1L);
    source.setCreatedDate(Date.from(LocalDateTime.of(2019, 12 ,31, 15, 16, 17)
      .toInstant(ZoneOffset.UTC)));
    source.setActor("A");
    reminder.setId(2L);
    reminder.setName("B");
    source.setReminder(reminder);

    final DriverEmailHistoryItemDto result = testedInstance.toDto(source);

    assertEquals(source.getId(), result.getId());
    assertEquals(source.getActor(), result.getActor());
    assertEquals(source.getReminder().getId(), result.getCommunicationTypeId());
    assertEquals(source.getReminder().getName(), result.getCommunicationType());
    assertEquals("12/31/2019 09:16 AM", result.getDate());
  }
}