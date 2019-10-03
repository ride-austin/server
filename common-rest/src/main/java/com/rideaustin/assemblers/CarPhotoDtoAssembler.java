package com.rideaustin.assemblers;

import org.springframework.stereotype.Component;

import com.rideaustin.model.Document;
import com.rideaustin.rest.model.CarPhotoDto;

@Component
public class CarPhotoDtoAssembler implements SingleSideAssembler<Document, CarPhotoDto> {
  @Override
  public CarPhotoDto toDto(Document document) {
    if (document == null) {
      return null;
    }
    return new CarPhotoDto(document.getId(), document.getDocumentUrl(), document.getDocumentType());
  }
}
