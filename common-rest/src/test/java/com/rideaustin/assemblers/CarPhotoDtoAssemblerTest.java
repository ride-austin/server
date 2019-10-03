package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.rest.model.CarPhotoDto;

public class CarPhotoDtoAssemblerTest {

  private CarPhotoDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new CarPhotoDtoAssembler();
  }

  @Test
  public void toDtoSkipsNull() {
    final CarPhotoDto result = testedInstance.toDto((Document) null);

    assertNull(result);
  }

  @Test
  public void toDtoSetsFields() {
    Document document = new Document();
    document.setId(1L);
    document.setDocumentUrl("Url");
    document.setDocumentType(DocumentType.CAR_PHOTO_FRONT);

    final CarPhotoDto result = testedInstance.toDto(document);

    assertEquals(document.getId(), result.getId());
    assertEquals(document.getDocumentUrl(), result.getPhotoUrl());
    assertEquals(document.getDocumentType(), result.getCarPhotoType());
  }
}