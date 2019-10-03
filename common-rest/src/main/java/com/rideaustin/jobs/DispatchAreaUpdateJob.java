package com.rideaustin.jobs;

import javax.inject.Inject;

import com.rideaustin.model.Area;
import com.rideaustin.service.areaqueue.AreaQueueUpdateService;
import com.rideaustin.service.areaqueue.AreaService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DispatchAreaUpdateJob extends BaseJob {

  @Setter(onMethod = @__(@Inject))
  private AreaQueueUpdateService areaQueueUpdateService;

  @Setter(onMethod = @__(@Inject))
  private AreaService areaService;

  @Override
  protected String getDescription() {
    return "Update active driver queued areas";
  }

  @Override
  protected void executeInternal() {
    areaService.getAllAreas().stream().filter(Area::isEnabled).map(Area::getId).forEach(areaId -> {
      try {
        areaQueueUpdateService.updateStatuses(areaId);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    });
  }
}
