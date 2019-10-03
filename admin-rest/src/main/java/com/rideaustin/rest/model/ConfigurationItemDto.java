package com.rideaustin.rest.model;

import com.rideaustin.filter.ClientType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class ConfigurationItemDto {

  @ApiModelProperty(example = "1", required = true)
  private final long id;

  @ApiModelProperty(example = "1", required = true)
  private final Long cityId;

  @ApiModelProperty(required = true)
  private final ClientType clientType;

  @ApiModelProperty(required = true)
  private final String configurationKey;

  @ApiModelProperty(required = true)
  private final Object configurationValue;

  @ApiModelProperty
  private final boolean isDefault;

  @ApiModelProperty
  private final String environment;

  public ConfigurationItemDto(long id, Long cityId, ClientType clientType, String configurationKey, Object configurationValue, boolean isDefault, String environment) {
    this.id = id;
    this.cityId = cityId;
    this.clientType = clientType;
    this.configurationKey = configurationKey;
    this.configurationValue = configurationValue;
    this.isDefault = isDefault;
    this.environment = environment;
  }

  public long getId() {
    return id;
  }

  public Long getCityId() {
    return cityId;
  }

  public ClientType getClientType() {
    return clientType;
  }

  public String getConfigurationKey() {
    return configurationKey;
  }

  public Object getConfigurationValue() {
    return configurationValue;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public String getEnvironment() {
    return environment;
  }
}
