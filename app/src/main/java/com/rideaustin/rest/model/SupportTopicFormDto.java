package com.rideaustin.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
public class SupportTopicFormDto {

  @ApiModelProperty(required = true)
  private long id;
  @ApiModelProperty(required = true)
  private String body;
  @ApiModelProperty(required = true)
  private String title;
  @ApiModelProperty(required = true)
  private String headerText;
  @ApiModelProperty(required = true)
  private String actionTitle;
  @ApiModelProperty(required = true)
  private String actionType;
  @ApiModelProperty(required = true)
  private List<Field> supportFields;

  @Getter
  @Setter
  @ApiModel
  @JsonIgnoreProperties({"mandatory"})
  public static class Field {
    @ApiModelProperty(required = true)
    private String fieldTitle;
    @ApiModelProperty
    private String fieldPlaceholder;
    @ApiModelProperty(required = true)
    private String fieldType;
    @ApiModelProperty(required = true)
    @JsonProperty("isMandatory")
    private boolean isMandatory;
    @ApiModelProperty(required = true)
    private String variable;
  }
}
