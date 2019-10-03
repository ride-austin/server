package com.rideaustin.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@Embeddable
@NoArgsConstructor
public class Address {

  @ApiModelProperty(required = true)
  @Column(name = "address", columnDefinition = "LONGTEXT")
  private String address;

  @ApiModelProperty
  @Column(name = "zip_code")
  private String zipCode;

  @Transient
  @ApiModelProperty(required = true)
  private Double lat;

  @Transient
  @ApiModelProperty(required = true)
  private Double lng;

  public Address(String address, String zipCode) {
    this.address = address;
    this.zipCode = zipCode;
  }

  public Address concat(String add) {
    address += add;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Address other = (Address) o;
    return Objects.equals(address, other.address)
      && Objects.equals(zipCode, other.zipCode);

  }

  @Override
  public int hashCode() {
    int result = address != null ? address.hashCode() : 0;
    result = 31 * result + (zipCode != null ? zipCode.hashCode() : 0);
    return result;
  }
}
