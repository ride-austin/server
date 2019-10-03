package com.rideaustin.events;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentStatus;

public class TNCCardUpdateEvent {

  private final Document card;
  private final DocumentStatus status;

  public TNCCardUpdateEvent(Document card, DocumentStatus status) {
    this.card = card;
    this.status = status;
  }

  public Document getCard() {
    return card;
  }

  public DocumentStatus getStatus() {
    return status;
  }
}
