package com.rideaustin.service;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;

public interface DocumentExpirationHandler {

  void handle(Document document);

  boolean supports(DocumentType documentType);

}
