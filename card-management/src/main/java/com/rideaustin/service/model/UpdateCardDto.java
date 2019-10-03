package com.rideaustin.service.model;

import lombok.Getter;

@Getter
public class UpdateCardDto {

  private final long id;
  private final boolean primary;
  private final String expMonth;
  private final String expYear;

  public UpdateCardDto(long id, boolean primary) {
    this(id, primary, null, null);
  }

  public UpdateCardDto(long id, boolean primary, String expMonth, String expYear) {
    this.id = id;
    this.primary = primary;
    this.expMonth = expMonth;
    this.expYear = expYear;
  }
}
