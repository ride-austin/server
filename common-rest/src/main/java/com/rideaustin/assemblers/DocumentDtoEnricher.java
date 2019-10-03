package com.rideaustin.assemblers;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.DocumentDto;
import com.rideaustin.service.thirdparty.S3StorageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DocumentDtoEnricher implements DTOEnricher<DocumentDto> {

  private final S3StorageService s3StorageService;

  @Override
  public DocumentDto enrich(DocumentDto source) {
    if (source == null) {
      return null;
    }

    if (source.getDocumentType().isPrivate()) {
      source.setDocumentUrl(s3StorageService.getSignedURL(source.getDocumentUrl()));
    } else {
      source.setDocumentUrl(source.getDocumentUrl());
    }
    return source;
  }
}
