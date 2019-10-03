package com.rideaustin.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
public class DocumentDto {

  private final long id;
  private final DocumentType documentType;

  private final DocumentStatus documentStatus;
  @Setter
  private String documentUrl;
  private final String name;
  private final String notes;
  private final Long cityId;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private final Date validityDate;

  @QueryProjection
  public DocumentDto(long id, DocumentType documentType, DocumentStatus documentStatus, String documentUrl, String name,
    String notes, Long cityId, Date validityDate) {
    this.id = id;
    this.documentType = documentType;
    this.documentStatus = documentStatus;
    this.documentUrl = documentUrl;
    this.name = name;
    this.notes = notes;
    this.cityId = cityId;
    this.validityDate = validityDate;
  }
}

