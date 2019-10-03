package com.rideaustin.jobs;

import javax.inject.Inject;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.GlobalExceptionEmailHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public abstract class BaseJob extends QuartzJobBean {

  @Inject
  private GlobalExceptionEmailHelper globalExceptionEmailHelper;

  protected abstract void executeInternal() throws com.rideaustin.jobs.JobExecutionException;

  protected abstract String getDescription();

  @Override
  protected final void executeInternal(JobExecutionContext context) throws JobExecutionException {
    String desc = getDescription();
    try {
      log.info("Starting {}", desc);
      executeInternal();
    } catch (Exception e) {
      log.info("Error while {}", desc, e);
      globalExceptionEmailHelper.processException(new ServerError("Error while executing job " + desc, e), null);
      throw new JobExecutionException(e);
    } finally {
      log.info("Finished {}", desc);
    }
  }
}
