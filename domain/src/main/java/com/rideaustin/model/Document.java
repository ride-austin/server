package com.rideaustin.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documents")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document extends BaseEntity {

  @ApiModelProperty
  @Enumerated(EnumType.STRING)
  @Column(name = "document_type")
  private DocumentType documentType;

  @ApiModelProperty
  @Enumerated(EnumType.STRING)
  @Column(name = "document_status")
  private DocumentStatus documentStatus = DocumentStatus.PENDING;

  @ApiModelProperty
  @Column(name = "document_url")
  private String documentUrl;

  @ApiModelProperty
  @Column(name = "name")
  private String name;

  @ApiModelProperty
  @Column(name = "notes")
  private String notes;

  @ApiModelProperty
  @Column(name = "city_id")
  private Long cityId;

  @ApiModelProperty
  @Temporal(TemporalType.DATE)
  @Column(name = "validity_date")
  private Date validityDate;

  @ApiModelProperty
  @Column(name = "removed", nullable = false)
  private boolean removed = Boolean.FALSE;

  @JsonIgnore
  public boolean isValidNow() {
    if (validityDate == null) {
      return true;
    }
    return validityDate.after(new Date());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}

