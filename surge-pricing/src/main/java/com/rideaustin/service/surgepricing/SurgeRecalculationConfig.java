package com.rideaustin.service.surgepricing;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rideaustin.service.config.SurgeMode;
import com.rideaustin.service.config.SurgeProvider;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = SurgeRecalculationConfig.class)
public class SurgeRecalculationConfig {


  static final String SURGE_EQUATION_MAPPING_KEY = "surgeEquationMapping";
  static final String SURGE_MODE_KEY = "surgeMode";
  static final String UTILIZATION_THRESHOLD_KEY = "utilizationThreshold";
  static final String MAX_AUTHORIZED_LIMITED_AUTO_VALUE_KEY = "maxAuthorizedLimitedAutoValue";
  static final String DEFAULT_AREA_MONITORING_PERIOD_KEY = "defaultAreaMonitoringPeriod";
  static final String SURGE_PROVIDER = "surgeProvider";

  @JsonIgnore
  private Map<String, Object> container = new HashMap<>();

  SurgeRecalculationConfig(Map<String, Object> map) {
    container.putAll(map);
  }

  SurgeRecalculationConfig() {
    put(SURGE_EQUATION_MAPPING_KEY, ImmutableMap.<String, BigDecimal>builder()
      .put(range(-1000, 99), BigDecimal.ONE)
      .put(range(100, 174), BigDecimal.valueOf(1.25))
      .put(range(175, 199), BigDecimal.valueOf(1.5))
      .put(range(200, 299), BigDecimal.valueOf(1.75))
      .put(range(300, 349), BigDecimal.valueOf(2.0))
      .put(range(350, 399), BigDecimal.valueOf(2.25))
      .put(range(400, 449), BigDecimal.valueOf(2.5))
      .put(range(450, 499), BigDecimal.valueOf(2.75))
      .put(range(500, 599), BigDecimal.valueOf(3.0))
      .put(range(600, 699), BigDecimal.valueOf(3.5))
      .put(range(700, 799), BigDecimal.valueOf(4.0))
      .put(range(800, 899), BigDecimal.valueOf(4.5))
      .put(range(900, Integer.MAX_VALUE), BigDecimal.valueOf(5.0))
      .build());
    put(SURGE_MODE_KEY, SurgeMode.LIMITED_AUTO.name());
    put(UTILIZATION_THRESHOLD_KEY, 0.65);
    put(DEFAULT_AREA_MONITORING_PERIOD_KEY, 1800000);
    put(MAX_AUTHORIZED_LIMITED_AUTO_VALUE_KEY, 3.0);
    put(SURGE_PROVIDER, SurgeProvider.STATS.name());
  }

  SurgeRecalculationConfig(String jsonString) {
    JsonParser jsonParser = new JsonParser();
    JsonObject str = jsonParser.parse(jsonString).getAsJsonObject();
    put(SURGE_MODE_KEY, str.get(SURGE_MODE_KEY).getAsString());
    put(UTILIZATION_THRESHOLD_KEY, str.get(UTILIZATION_THRESHOLD_KEY).getAsFloat());
    put(DEFAULT_AREA_MONITORING_PERIOD_KEY, str.get(DEFAULT_AREA_MONITORING_PERIOD_KEY).getAsLong());
    put(MAX_AUTHORIZED_LIMITED_AUTO_VALUE_KEY, str.get(MAX_AUTHORIZED_LIMITED_AUTO_VALUE_KEY).getAsFloat());
    JsonObject jsonObject = str.get(SURGE_EQUATION_MAPPING_KEY).getAsJsonObject();
    Set<Entry<String, JsonElement>> entries = jsonObject.entrySet();
    Map<String, BigDecimal> map = entries.stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getAsBigDecimal()));
    ImmutableMap immutableMap = ImmutableMap.builder().putAll(map).build();
    put(SURGE_EQUATION_MAPPING_KEY, immutableMap);
    put(SURGE_PROVIDER, str.get(SURGE_PROVIDER).getAsString());
  }

  @JsonProperty
  @ApiModelProperty(required = true)
  Map<Range<BigDecimal>, BigDecimal> getSurgeEquationMapping() {
    Map<String, Object> rawMap = (Map<String, Object>) get(SURGE_EQUATION_MAPPING_KEY);
    return rawMap.entrySet().stream().collect(Collectors.toMap(
      e -> range(e.getKey()),
      e -> BigDecimal.valueOf(Double.valueOf(e.getValue().toString()))
    ));
  }

  @JsonProperty
  @ApiModelProperty(required = true)
  SurgeMode getSurgeMode() {
    return SurgeMode.valueOf((String) get(SURGE_MODE_KEY));
  }

  @JsonProperty
  @ApiModelProperty(required = true)
  SurgeProvider getSurgeProvider() {
    return SurgeProvider.valueOf((String) get(SURGE_PROVIDER));
  }

  @JsonProperty
  @ApiModelProperty(required = true)
  BigDecimal getUtilizationThreshold() {
    return BigDecimal.valueOf((Double) get(UTILIZATION_THRESHOLD_KEY));
  }

  @JsonProperty
  @ApiModelProperty(required = true)
  BigDecimal getMaxAuthorizedLimitedAutoValue() {
    return BigDecimal.valueOf((Double) get(MAX_AUTHORIZED_LIMITED_AUTO_VALUE_KEY));
  }

  @JsonProperty
  @ApiModelProperty(required = true)
  int getDefaultAreaMonitoringPeriod() {
    return (int) get(DEFAULT_AREA_MONITORING_PERIOD_KEY);
  }

  public Map<String, Object> asMap() {
    return container;
  }

  private Object get(String key) {
    return container.get(key);
  }

  private void put(String key, Object value) {
    container.put(key, value);
  }

  static String range(int from, int to) {
    return Range.between(BigDecimal.valueOf(from), BigDecimal.valueOf(to)).toString();
  }

  static Range<BigDecimal> range(String string) {
    Matcher matcher = Pattern.compile("\\[(-?[0-9]+)\\.\\.([0-9]+)]").matcher(string);
    if (matcher.matches()) {
      BigDecimal start = BigDecimal.valueOf(Double.valueOf(matcher.group(1)));
      BigDecimal end = BigDecimal.valueOf(Double.valueOf(matcher.group(2)));
      return Range.between(start, end);
    }
    return Range.is(BigDecimal.ZERO);
  }

  public void setSurgeMode(SurgeMode surgeMode) {
    put(SurgeRecalculationConfig.SURGE_MODE_KEY, surgeMode);
  }
}
