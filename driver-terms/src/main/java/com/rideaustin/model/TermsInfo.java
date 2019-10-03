package com.rideaustin.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class TermsInfo {

  @JsonProperty("currentTermsId")
  private final long id;
  @JsonProperty("currentTermsUrl")
  private final String url;
  @JsonProperty("currentTermsIsMandatory")
  private final boolean mandatory;
  @JsonProperty("currentTermsVersion")
  private final String version;
  @JsonProperty("currentTermsPublicationDate")
  private final Date publicationDate;

  @QueryProjection
  public TermsInfo(long id, String url, boolean mandatory, String version, Date publicationDate) {
    this.id = id;
    this.url = url;
    this.mandatory = mandatory;
    this.version = version;
    this.publicationDate = publicationDate;
  }
}
