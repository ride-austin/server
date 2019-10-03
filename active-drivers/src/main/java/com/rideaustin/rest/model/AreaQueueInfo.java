package com.rideaustin.rest.model;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rideaustin.model.AreaQueueEntry;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class AreaQueueInfo {

  @ApiModelProperty(required = true)
  private Map<String, List<AreaQueueDriverInfo>> entries;

  public AreaQueueInfo(Map<String, List<AreaQueueEntry>> entries) {
    this.entries = new HashMap<>();
    for (Map.Entry<String, List<AreaQueueEntry>> entry : entries.entrySet()) {
      this.entries.put(entry.getKey(), entry.getValue().stream().sorted(Comparator.comparing(AreaQueueEntry::getCreatedDate)).map(AreaQueueDriverInfo::new).collect(Collectors.toList()));
    }
  }

  @Getter
  @ApiModel
  public static class AreaQueueDriverInfo {
    @ApiModelProperty(required = true, example = "1")
    private final Long driver;
    @ApiModelProperty(required = true)
    private final String name;
    @ApiModelProperty(required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private final Date createdOn;
    @ApiModelProperty(required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private final Date lastUpdated;

    AreaQueueDriverInfo(AreaQueueEntry e) {
      this.driver = e.getActiveDriver().getDriver().getId();
      this.name = String.format("%s %s", e.getActiveDriver().getDriver().getFirstname(), e.getActiveDriver().getDriver().getLastname());
      this.lastUpdated = e.getLastPresentInQueue();
      this.createdOn = e.getCreatedDate();
    }
  }
}