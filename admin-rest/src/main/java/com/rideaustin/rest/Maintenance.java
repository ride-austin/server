package com.rideaustin.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.ExternalEndpoint;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.JobDto;
import com.rideaustin.service.SchedulerService;
import com.rideaustin.service.recovery.ActiveDriversRecoveryService;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@CheckedTransactional
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequestMapping("/rest/maintenance")
public class Maintenance {

  private final SchedulerService schedulerService;
  private final ActiveDriversRecoveryService activeDriversRecoveryService;
  private final Environment environment;

  private List<String> approvedEnvironmentKeysToVisible;

  @Inject
  public Maintenance(SchedulerService schedulerService, ActiveDriversRecoveryService activeDriversRecoveryService,
    Environment environment) {
    this.schedulerService = schedulerService;
    this.activeDriversRecoveryService = activeDriversRecoveryService;
    this.environment = environment;
    approvedEnvironmentKeysToVisible = Arrays.asList(environment.getProperty("maintenance.approved.environment.to.display",
      "dispatch,jobs,ride,area,active_driver,driver,surge_pricing")
      .split(","));

  }

  @ExternalEndpoint
  @GetMapping(path = "jobs")
  public Map<String, List<JobDto>> listCurrentJobs() throws SchedulerException {
    return schedulerService.listCurrentJobs();
  }

  @ExternalEndpoint
  @PostMapping(path = "jobs/{jobGroup}:{jobName}/execute")
  public void executeJob(@PathVariable String jobGroup, @PathVariable String jobName) throws RideAustinException {
    validateJobOperation(jobName, jobGroup);
    schedulerService.triggerJob(jobName, jobGroup);
  }

  @ExternalEndpoint
  @PostMapping(path = "jobs/{jobGroup}:{jobName}/pause")
  public void pauseJob(@PathVariable String jobGroup, @PathVariable String jobName) throws RideAustinException {
    validateJobOperation(jobName, jobGroup);
    schedulerService.pauseJob(jobName, jobGroup);
  }

  @ExternalEndpoint
  @PostMapping(path = "jobs/{jobGroup}:{jobName}/resume")
  public void resumeJob(@PathVariable String jobGroup, @PathVariable String jobName) throws RideAustinException {
    validateJobOperation(jobName, jobGroup);
    schedulerService.resumeJob(jobName, jobGroup);
  }

  @ExternalEndpoint
  @PostMapping(path = "jobs/execute")
  public void executeJobWithParams(@RequestParam String jobClass, @RequestParam Map<String, Object> dataMap) throws ServerError {
    try {
      Class jobClassObj = Class.forName(jobClass);
      schedulerService.triggerJob(jobClassObj, dataMap);
    } catch (Exception e) {
      throw new ServerError(e);
    }
  }

  @WebClientEndpoint
  @PostMapping("rides/stuck")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void executeUnstuckRides() {
    activeDriversRecoveryService.cleanUpRidingDrivers();
  }

  @ExternalEndpoint
  @PostMapping(value = "env/value")
  public ResponseEntity<String> environmentValue(@RequestParam String key) {
    if (isNotHidden(key)) {
      String value = environment.getProperty(key);
      if (value != null) {
        return new ResponseEntity<>(value, HttpStatus.OK);
      }
    }
    return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
  }

  private void validateJobOperation(String jobName, String jobGroup) throws BadRequestException, ServerError {
    if (StringUtils.isBlank(jobName)) {
      throw new BadRequestException("Job name cannot be blank");
    }

    if(!schedulerService.checkIfExists(jobName, jobGroup)){
      throw new BadRequestException(String.format("Job cannot be found with %s:%s", jobGroup, jobName));
    }
  }

  private boolean isNotHidden(String key) {
    List<String> started = approvedEnvironmentKeysToVisible.stream().filter(key::startsWith).collect(Collectors.toList());
    return !started.isEmpty();
  }

}
