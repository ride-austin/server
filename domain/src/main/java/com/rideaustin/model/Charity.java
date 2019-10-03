package com.rideaustin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@Table(name = "charities")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Charity {

  @Id
  @ApiModelProperty(required = true, example = "1")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private long id;

  @Column
  @ApiModelProperty(required = true)
  private String name;

  @Lob
  @ApiModelProperty
  private String description;

  @Column(name = "image_url")
  @ApiModelProperty(required = true)
  private String imageUrl;

  @Column(name = "city_bitmask")
  @ApiModelProperty(required = true)
  private Integer cityBitmask;

  @Column(name = "order")
  @ApiModelProperty(required = true)
  private Integer order;

  @Column(name = "enabled")
  @ApiModelProperty(required = true)
  private boolean enabled;

}
