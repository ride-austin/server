package com.rideaustin.assemblers;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.rest.model.DirectConnectHistoryDto;
import com.rideaustin.service.DocumentService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DirectConnectHistoryDtoEnricher implements DTOEnricher<DirectConnectHistoryDto> {

  private final DocumentService documentService;

  @Override
  public DirectConnectHistoryDto enrich(DirectConnectHistoryDto source) {
    if (source == null) {
      return null;
    }

    Optional.ofNullable(documentService.findAvatarDocument(source.getDriverId(), DocumentType.DRIVER_PHOTO))
      .map(Document::getDocumentUrl)
      .ifPresent(source::setPhotoURL);

    return source;
  }

}
