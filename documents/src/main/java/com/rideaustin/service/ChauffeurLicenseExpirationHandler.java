package com.rideaustin.service;

import java.util.Collections;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.service.user.DriverTypeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChauffeurLicenseExpirationHandler implements DocumentExpirationHandler {

  private final DocumentDslRepository documentDslRepository;

  @Override
  @Transactional
  public void handle(Document document) {
    Driver driver = documentDslRepository.findDriver(document);
    if (driver == null) {
      log.error(String.format("Exception: no driver found for document %d", document.getId()));
    } else {
      driver.setGrantedDriverTypesBitmask(driver.getGrantedDriverTypesBitmask() ^ DriverTypeUtils.toBitMask(Collections.singleton(DriverType.DIRECT_CONNECT)));
      documentDslRepository.saveAny(driver);
    }
  }

  @Override
  public boolean supports(DocumentType documentType) {
    return DocumentType.CHAUFFEUR_LICENSE.equals(documentType);
  }
}
